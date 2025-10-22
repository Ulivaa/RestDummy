package com.LT.restDummy.helper;

import com.LT.restDummy.victoria.VictoriaWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Планирует отложенную отправку ответа для REST-сервиса.
 * Работает поверх инжектированного ScheduledExecutorService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseDelay {

    /** Пул создаётся в конфиге PropertyBeen#responseDelayExecutor() */
    @Qualifier("responseDelayExecutor")
    private final ScheduledExecutorService scheduler;

    /** Инжектированный отправитель метрик (без статических вызовов) */
    private final VictoriaWriter victoriaWriter;

    /**
     * Планирует формирование ответа через указанную задержку.
     *
     * @param delay         задержка в миллисекундах (отрицательная трактуется как 0)
     * @param responseBody  тело ответа
     * @param operationName имя операции для метрик (может быть null/пустым)
     * @param httpHeaders   заголовки ответа (если null — будет пустой набор)
     */
    public CompletableFuture<ResponseEntity<String>> scheduleResponse(long delay,
                                                                      String responseBody,
                                                                      String operationName,
                                                                      HttpHeaders httpHeaders) {
        final CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();
        final HttpHeaders safeHeaders = (httpHeaders != null) ? httpHeaders : new HttpHeaders();
        final long effectiveDelay = Math.max(0L, delay);

        scheduler.schedule(() -> {
            try {
                ResponseEntity<String> response =
                        new ResponseEntity<>(responseBody, safeHeaders, HttpStatus.OK);
                future.complete(response);
                log.info("RESPONSE (op='{}', delayMs={}): {}", operationName, effectiveDelay, responseBody);
            } finally {
                // метрики — best-effort
                try {
                    if (operationName != null && !operationName.isEmpty() && victoriaWriter != null) {
                        // экземплярный вызов вместо статического
                        victoriaWriter.sendMetrics(operationName, effectiveDelay);
                    }
                } catch (Exception e) {
                    log.error("Exception while sending metrics", e);
                }
            }
        }, effectiveDelay, TimeUnit.MILLISECONDS);

        return future;
    }
}
