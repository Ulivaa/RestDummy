package com.LT.restDummy.scheduler;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeastOnce;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@TestPropertySource(properties = {
        "scheduler.availability.rate=1000",
        "scheduler.delay.rate=1000"
})
public class SchedulerTest {

    @SpyBean
    private AvailabilitySchedulerService availabilitySchedulerService;

    @SpyBean
    private DelaySchedulerService delaySchedulerService;

    @Autowired
    private Scheduler scheduler;

    @Test
    public void testSchedulerTriggersAvailabilityAndDelayChecks() {
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                Mockito.verify(availabilitySchedulerService, atLeastOnce()).checkAndScheduleAvailability()
        );

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                Mockito.verify(delaySchedulerService, atLeastOnce()).checkAndScheduleDelay()
        );
    }
}
