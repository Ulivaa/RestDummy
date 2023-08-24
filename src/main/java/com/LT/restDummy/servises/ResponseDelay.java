package com.LT.restDummy.servises;

import com.LT.restDummy.influx.InfluxWriter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 Класс отвечает за выставление задержки для rest сервиса
 */
@Slf4j
public class ResponseDelay {

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @SneakyThrows
    public static CompletableFuture<ResponseEntity<String>> scheduleResponse(long delay,
                                                                             String responseBody,
                                                                             String operationName,
                                                                             HttpHeaders httpHeaders) {
        CompletableFuture<ResponseEntity<String>> response = new CompletableFuture<>();
        scheduler.schedule(() -> {
            ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, httpHeaders, HttpStatus.OK);
            response.complete(responseEntity);
            log.info("RESPONSE: " + responseBody);
            InfluxWriter.getInstance().addPoint(operationName);
        }, delay, TimeUnit.MILLISECONDS);
        return response;
    }
}