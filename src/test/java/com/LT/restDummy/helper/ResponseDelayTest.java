package com.LT.restDummy.helper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ResponseDelayTest {

    @Autowired
    private ResponseDelay responseDelay; // бин, у которого есть scheduleResponse(...)

    @Test
    void shouldReturnResponseWithDelay() {
        long start = System.currentTimeMillis();

        responseDelay
                .scheduleResponse(6000, "responseMessage", "service1", HttpHeaders.EMPTY)
                .join();

        long elapsed = System.currentTimeMillis() - start;
        // небольшой люфт на планирование потока — при желании можно оставить 5900–5950
        assertTrue(elapsed >= 6000, "Elapsed ms = " + elapsed);
    }
}
