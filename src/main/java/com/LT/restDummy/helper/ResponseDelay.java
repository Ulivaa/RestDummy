package com.LT.restDummy.helper;

import com.LT.restDummy.victoria.VictoriaWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Класс отвечает за выставление задержки для REST-сервиса.
 */
@Slf4j
public final class ResponseDelay {

    private static final AtomicInteger THREAD_SEQ = new AtomicInteger(1);

    private static final ThreadFactory DELAY_THREAD_FACTORY = r -> {
        Thread t = new Thread(r, "response-delay-" + THREAD_SEQ.getAndIncrement());
        t.setDaemon(true); // чтобы тесты/приложение не висели из-за пула
        t.setUncaughtExceptionHandler((thr, ex) ->
                log.error("Uncaught exception in ResponseDelay scheduler thread {}", thr.getName(), ex));
        return t;
    };

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(2, DELAY_THREAD_FACTORY);

    private ResponseDelay() {
        // utility
    }

    /**
     * Планирует формирование ответа через указанную задержку.
     *
     * @param delay         задержка в миллисекундах (если отрицательная — будет интерпретирована как 0)
     * @param responseBody  тело ответа
     * @param operationName имя операции для метрик (может быть null/пустым)
     * @param httpHeaders   заголовки ответа (если null — возьмём пустые)
     */
    public static CompletableFuture<ResponseEntity<String>> scheduleResponse(long delay,
                                                                             String responseBody,
                                                                             String operationName,
                                                                             HttpHeaders httpHeaders) {
        final CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();
        final HttpHeaders safeHeaders = (httpHeaders != null) ? httpHeaders : new HttpHeaders();
        final long effectiveDelay = (delay < 0L) ? 0L : delay;

        scheduler.schedule(() -> {
            try {
                ResponseEntity<String> response =
                        new ResponseEntity<String>(responseBody, safeHeaders, HttpStatus.OK);
                future.complete(response);
                log.info("RESPONSE (op='{}', delayMs={}): {}", operationName, effectiveDelay, responseBody);
            } finally {
                // метрики — best-effort, не валим поток
                try {
                    if (operationName != null && !operationName.isEmpty()) {
                        VictoriaWriter.sendMetrics(operationName, effectiveDelay);
                    }
                } catch (Exception e) {
                    log.error("Exception while sending metrics", e);
                }
            }
        }, effectiveDelay, TimeUnit.MILLISECONDS);

        return future;
    }
}
