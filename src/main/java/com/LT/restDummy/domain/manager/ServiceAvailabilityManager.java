package com.LT.restDummy.domain.manager;

import com.LT.restDummy.domain.model.StubService;

import java.time.LocalDateTime;

public class ServiceAvailabilityManager {

    private final ServiceRegistry registry;

    public ServiceAvailabilityManager(ServiceRegistry registry) {
        this.registry = registry;
    }

    public boolean isAvailable(String name) {
        return registry.get(name).isAvailable();
    }

    public void setAvailable(String name, boolean available) {
        registry.get(name).setAvailable(available);
    }

    public void setAvailableToAll(boolean available) {
        for (StubService s : registry.getAll()) {
            s.setAvailable(available);
        }
    }

    public void scheduleAvailability(String name, LocalDateTime scheduler) {
        registry.get(name).setAvailabilityScheduler(scheduler);
    }

    public LocalDateTime getAvailabilityScheduler(String name) {
        return registry.get(name).getAvailabilityScheduler();
    }
}

