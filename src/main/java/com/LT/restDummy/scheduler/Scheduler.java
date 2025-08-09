package com.LT.restDummy.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Scheduler {

    private final AvailabilitySchedulerService availabilitySchedulerService;
    private final DelaySchedulerService delaySchedulerService;

    @Scheduled(fixedRateString = "${scheduler.availability.rate:60000}")
    public void checkServiceAvailability() {
        availabilitySchedulerService.checkAndScheduleAvailability();
    }

    @Scheduled(fixedRateString = "${scheduler.delay.rate:60000}")
    public void checkServiceDelay() {
        delaySchedulerService.checkAndScheduleDelay();
    }
}
