package com.LT.restDummy.domain.manager;

import com.LT.restDummy.domain.model.StubService;

import java.time.LocalDateTime;

public class ServiceDelayManager {

    private final ServiceRegistry registry;

    public ServiceDelayManager(ServiceRegistry registry) {
        this.registry = registry;
    }

    public long getDelay(String name) {
        return registry.get(name).getDelayConfig().getCurrentDelay();
    }

    public void setDelay(String name, long delay) {
        registry.get(name).getDelayConfig().setCurrentDelay(delay);
    }

    public void setDefaultDelays() {
        for (StubService s : registry.getAll()) {
            s.getDelayConfig().setCurrentDelay(s.getDelayConfig().getDefaultDelay());
        }
    }

    public long getTimeout(String name) {
        return registry.get(name).getDelayConfig().getTimeout();
    }

    public long getDelayForScheduler(String name) {
        return registry.get(name).getDelayConfig().getDelayForScheduler();
    }

    public void setDelayForScheduler(String name, long delay) {
        registry.get(name).getDelayConfig().setDelayForScheduler(delay);
    }

    public LocalDateTime getSchedulerToDelay(String name) {
        return registry.get(name).getDelayConfig().getSchedulerToDelay();
    }

    public void setSchedulerToDelay(String name, LocalDateTime scheduler) {
        registry.get(name).getDelayConfig().setSchedulerToDelay(scheduler);
    }

    public long getDefaultDelay(String name) {
        return registry.get(name).getDelayConfig().getDefaultDelay();
    }

    public long calculateMinus10PercentDelay(String serviceName) {
        long timeout = registry.get(serviceName).getDelayConfig().getTimeout();
        return (long) (timeout * 0.9);
    }


    public void applyMinus10PercentToAll() {
        for (StubService service : registry.getAll()) {
            long timeout = service.getDelayConfig().getTimeout();
            if (timeout > 0) {
                service.getDelayConfig().setCurrentDelay((long) (timeout * 0.9));
            }
        }
    }
}