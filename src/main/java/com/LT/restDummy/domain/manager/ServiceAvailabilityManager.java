package com.LT.restDummy.domain.manager;

import com.LT.restDummy.domain.model.StubService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class ServiceAvailabilityManager {

    private final ServiceRegistry registry;

    public ServiceAvailabilityManager(ServiceRegistry registry) {
        this.registry = registry;
    }

    public boolean isAvailable(String name) {
        return service(name).isAvailable();
    }

    public void setAvailable(String name, boolean available) {
        service(name).setAvailable(available);
    }

    public void setAvailableToAll(boolean available) {
        for (StubService s : registry.getAll()) {
            s.setAvailable(available);
        }
    }

    public void scheduleAvailability(String name, LocalDateTime scheduler) {
        service(name).setAvailabilityScheduler(scheduler);
    }

    public LocalDateTime getAvailabilityScheduler(String name) {
        return service(name).getAvailabilityScheduler();
    }

    // --- helpers ---
    private StubService service(String name) {
        Objects.requireNonNull(name, "service name must not be null");
        // registry.get(name) бросит ServiceNotFoundException для неизвестного сервиса — как и раньше
        return registry.get(name);
    }
}
