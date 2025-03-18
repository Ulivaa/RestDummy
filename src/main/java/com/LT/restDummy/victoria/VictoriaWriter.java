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

@Service
@Slf4j
public class VictoriaWriter {

    private static RestTemplate restTemplate;
    private static String url;
    private static String application;
    private static String channel;

    // Карта для хранения количества запросов (метрика mock_requests_total)
    private static final Map<String, Integer> mockRequestCounters = new ConcurrentHashMap<>();
    private static final Map<String, Integer> lastSentCounters = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastActivityTimestamp = new ConcurrentHashMap<>(); // Последняя активность

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public VictoriaWriter(
            RestTemplate restTemplate,
            @Value("${victoria.url}") String victoriaUrl,
            @Value("${subsystem}") String application,
            @Value("${channel}") String channel) {
        VictoriaWriter.restTemplate = restTemplate;
        VictoriaWriter.url = victoriaUrl;
        VictoriaWriter.application = application;
        VictoriaWriter.channel = channel;
    }

    @PostConstruct
    public void init() {
// Отправка mock_requests_total раз в 30 секунд
        scheduler.scheduleAtFixedRate(VictoriaWriter::sendMockRequestsTotalMetrics, 30, 30, TimeUnit.SECONDS);
// Очистка неактивных счетчиков раз в 30 минут
        scheduler.scheduleAtFixedRate(VictoriaWriter::clearOldCounters, 30, 30, TimeUnit.MINUTES);
    }

    public static void sendMetrics(String operationName, double responseTime) {
        try {
            long currentTime = Instant.now().getEpochSecond();

// 1️⃣ Округляем responseTime до 1 знака после запятой
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            DecimalFormat df = new DecimalFormat("0.0", symbols);
            String formattedValue = df.format(responseTime);

// 2️⃣ Формируем строку mock_response_time_ms
            String durationMetric = String.format(
                    "mock_response_time_ms{application=\"%s\", channel=\"%s\", operation=\"%s\"} %s",
                    application, channel, operationName, formattedValue);

            log.info("📤 Sending Prometheus metric:\n{}", durationMetric);

// 3️⃣ Увеличиваем mock_requests_total для конкретного сервиса (Используем Integer::sum)
            mockRequestCounters.merge(operationName, 1, Integer::sum);
            lastActivityTimestamp.put(operationName, currentTime);

// 4️⃣ Заголовки запроса
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);

            HttpEntity requestEntity = new HttpEntity<>(durationMetric, headers);

// 5️⃣ Отправка mock_response_time_ms в VictoriaMetrics
            ResponseEntity response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

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
        try {
            for (Map.Entry<String, Integer> entry : mockRequestCounters.entrySet()) {
                String operationName = entry.getKey();
                int currentValue = entry.getValue() != null ? entry.getValue() : 0; // ✅ Безопасное приведение к int
                int lastValue = lastSentCounters.getOrDefault(operationName, 0);

// Отправляем только если значение изменилось
                if (currentValue != lastValue) {
                    String mockTotalMetric = String.format(
                            "mock_requests_total{application=\"%s\", channel=\"%s\", operation=\"%s\"} %d",
                            application, channel, operationName, currentValue);

                    log.info("📤 Sending accumulated mock_requests_total metric:\n{}", mockTotalMetric);

// Заголовки запроса
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.TEXT_PLAIN);

                    HttpEntity requestEntity = new HttpEntity<>(mockTotalMetric, headers);
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

                    if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                        log.info("✅ mock_requests_total successfully sent.");
                        lastSentCounters.put(operationName, currentValue);
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
        long currentTime = Instant.now().getEpochSecond();
        long oneHourAgo = currentTime - 3600; // 1 час назад

        boolean hasRecentActivity = lastActivityTimestamp.values().stream().anyMatch(ts -> ts > oneHourAgo);

        if (!hasRecentActivity) {
            log.info("🗑 No activity in the last hour, clearing all counters.");
            mockRequestCounters.clear();
            lastSentCounters.clear();
            lastActivityTimestamp.clear();
        }
    }
}