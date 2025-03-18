package com.LT.restDummy.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.LocalTime;

public class ResponseDelayTest {

    @Test
    public void shouldReturnResponseWithDelay() {
        LocalTime localTimeBefore = LocalTime.now();
        ResponseDelay.scheduleResponse(6000, "responseMessage", "service1", HttpHeaders.EMPTY).join();
        LocalTime localTimeAfter = LocalTime.now();
        long delay = localTimeAfter.getSecond() - localTimeBefore.getSecond();
        Assertions.assertTrue(delay >= 6);
    }
}