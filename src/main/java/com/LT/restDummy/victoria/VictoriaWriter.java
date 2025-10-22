package com.LT.restDummy.victoria;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class VictoriaWriter {

    private static volatile RestTemplate restTemplate;
    private static volatile String url;
    private static volatile String application;
    private static volatile String channel;

    // глобальный флаг — реально ли отправляем метрики
    private static volatile boolean enabled = false;

    // значение флага из настроек (дефолт: true, чтобы без указания было ВКЛ)
    @Value("${victoria.enabled:true}")
    private boolean enabledProperty;

    private static final Map<String, Integer> mockRequestCounters = new ConcurrentHashMap<>();
    private static final Map<String, Integer> lastSentCounters = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastActivityTimestamp = new ConcurrentHashMap<>();

    private static final AtomicInteger THREAD_SEQ = new AtomicInteger(1);
    private static final ThreadFactory SCHEDULER_TF = r -> {
        Thread t = new Thread(r, "victoria-writer-" + THREAD_SEQ.getAndIncrement());
        t.setDaemon(true);
        t.setUncaughtExceptionHandler((thr, ex) ->
                log.error("Uncaught exception in VictoriaWriter thread {}", thr.getName(), ex));
        return t;
    };

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1, SCHEDULER_TF);

    public VictoriaWriter(
            RestTemplate restTemplate,
            @Value("${victoria.url:}") String victoriaUrl,
            @Value("${subsystem:}") String application,
            @Value("${channel:}") String channel
    ) {
        VictoriaWriter.restTemplate = restTemplate;
        VictoriaWriter.url = victoriaUrl;
        VictoriaWriter.application = application;
        VictoriaWriter.channel = channel;
    }

    @PostConstruct
    public void init() {
        // включаем, если:
        // 1) property не выключает (enabledProperty == true, а он по умолчанию true)
        // 2) есть базовая конфигурация (url не пустой и есть restTemplate)
        boolean hasMinimalConfig = restTemplate != null && url != null && !url.trim().isEmpty();
        enabled = enabledProperty && hasMinimalConfig;

        if (!enabledProperty) {
            log.info("VictoriaWriter is DISABLED via property: victoria.enabled=false");
            return;
        }

        if (!hasMinimalConfig) {
            log.warn("VictoriaWriter is DISABLED (missing minimal config: restTemplate or victoria.url). Metrics will be skipped.");
            return;
        }

        log.info("VictoriaWriter is ENABLED. Sending metrics to {}", url);

        // Отправка mock_requests_total раз в 30 секунд
        scheduler.scheduleAtFixedRate(VictoriaWriter::sendMockRequestsTotalMetrics, 30, 30, TimeUnit.SECONDS);
        // Очистка неактивных счетчиков раз в 30 минут
        scheduler.scheduleAtFixedRate(VictoriaWriter::clearOldCounters, 30, 30, TimeUnit.MINUTES);
    }

    public static void sendMetrics(String operationName, double responseTimeMs) {
        if (!enabled) return;
        try {
            final String op = (operationName != null) ? operationName : "unknown";
            final long nowSec = Instant.now().getEpochSecond();

            // округляем до 1 знака после запятой (US-десятичный разделитель)
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            DecimalFormat df = new DecimalFormat("0.0", symbols);
            String formattedValue = df.format(responseTimeMs);

            // mock_response_time_ms
            String durationMetric = String.format(
                    "mock_response_time_ms{application=\"%s\", channel=\"%s\", operation=\"%s\"} %s",
                    safe(application), safe(channel), op, formattedValue);

            log.info("📤 Sending Prometheus metric:\n{}", durationMetric);

            // счётчики и активность
            mockRequestCounters.merge(op, 1, Integer::sum);
            lastActivityTimestamp.put(op, nowSec);

            // заголовки
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            HttpEntity<String> requestEntity = new HttpEntity<>(durationMetric, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                log.info("✅ mock_response_time_ms successfully sent.");
            } else {
                log.error("❌ Failed to send mock_response_time_ms! Response code: {} | Body: {}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("❌ Exception while sending metrics: {}", e.getMessage(), e);
        }
    }

    private static void sendMockRequestsTotalMetrics() {
        if (!enabled) return;
        try {
            for (Map.Entry<String, Integer> entry : mockRequestCounters.entrySet()) {
                final String op = entry.getKey();
                final int currentValue = entry.getValue() != null ? entry.getValue() : 0;
                final int lastValue = lastSentCounters.getOrDefault(op, 0);

                // отправляем только если значение изменилось
                if (currentValue != lastValue) {
                    String mockTotalMetric = String.format(
                            "mock_requests_total{application=\"%s\", channel=\"%s\", operation=\"%s\"} %d",
                            safe(application), safe(channel), op, currentValue);

                    log.info("📤 Sending accumulated mock_requests_total metric:\n{}", mockTotalMetric);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.TEXT_PLAIN);
                    HttpEntity<String> requestEntity = new HttpEntity<>(mockTotalMetric, headers);

                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
                    if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                        log.info("✅ mock_requests_total successfully sent.");
                        lastSentCounters.put(op, currentValue);
                    } else {
                        log.error("❌ Failed to send mock_requests_total! Response code: {} | Body: {}",
                                response.getStatusCode(), response.getBody());
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Exception while sending mock_requests_total metrics: {}", e.getMessage(), e);
        }
    }

    private static void clearOldCounters() {
        if (!enabled) return;
        long currentTime = Instant.now().getEpochSecond();
        long oneHourAgo = currentTime - 3600;

        boolean hasRecentActivity = lastActivityTimestamp.values().stream()
                .filter(ts -> ts != null && ts > oneHourAgo)
                .findAny()
                .isPresent();

        if (!hasRecentActivity) {
            log.info("🗑 No activity in the last hour, clearing all counters.");
            mockRequestCounters.clear();
            lastSentCounters.clear();
            lastActivityTimestamp.clear();
        }
    }

    private static String safe(String s) {
        return (s != null) ? s : "null";
    }
}
