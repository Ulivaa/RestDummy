package com.LT.restDummy.domain.delay;

import java.time.LocalDateTime;

public class DelayConfig {
    public static final LocalDateTime DEFAULT_DATE = LocalDateTime.of(2000, 1, 1, 1, 1);

    private Long timeout;
    private Long defaultDelay;
    private Long currentDelay;
    private LocalDateTime schedulerToDelay = DEFAULT_DATE;
    private Long delayForScheduler;

    public DelayConfig(Long defaultDelay, Long timeout) {
        this.defaultDelay = defaultDelay;
        this.currentDelay = defaultDelay;
        this.timeout = timeout;
    }

    public void setDelayScheduler(LocalDateTime schedulerToDelay, Long delayForScheduler) {
        this.schedulerToDelay = schedulerToDelay;
        this.delayForScheduler = delayForScheduler;
    }

    public Long getEffectiveDelay(LocalDateTime now) {
        if (schedulerToDelay.isAfter(now)) {
            return delayForScheduler != null ? delayForScheduler : currentDelay;
        }
        return currentDelay;
    }

    public void setDelayForScheduler(Long delayForScheduler) {
        this.delayForScheduler = delayForScheduler;
    }

    public void setSchedulerToDelay(LocalDateTime schedulerToDelay) {
        this.schedulerToDelay = schedulerToDelay;
    }



    public Long getTimeout() {
        return timeout;
    }

    public void setCurrentDelay(Long delay) {
        this.currentDelay = delay;
    }

    public Long getCurrentDelay() {
        return currentDelay;
    }

    public Long getDefaultDelay() {
        return defaultDelay;
    }

    public LocalDateTime getSchedulerToDelay() {
        return schedulerToDelay;
    }

    public Long getDelayForScheduler() {
        return delayForScheduler;
    }
}
