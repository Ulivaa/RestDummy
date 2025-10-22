package com.LT.restDummy.domain.manager;

import com.LT.restDummy.domain.model.StubService;
import com.LT.restDummy.domain.delay.DelayConfig;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class ServiceDelayManager {

    private final ServiceRegistry registry;

    public ServiceDelayManager(ServiceRegistry registry) {
        this.registry = registry;
    }

    public long getDelay(String name) {
        return delayCfg(name).getCurrentDelay();
    }

    public void setDelay(String name, long delay) {
        delayCfg(name).setCurrentDelay(Math.max(0L, delay));
    }

    public void setDefaultDelays() {
        for (StubService s : registry.getAll()) {
            DelayConfig cfg = s.getDelayConfig();
            Objects.requireNonNull(cfg, "DelayConfig must not be null for service: " + s.getName());
            cfg.setCurrentDelay(cfg.getDefaultDelay());
        }
    }

    public long getTimeout(String name) {
        return delayCfg(name).getTimeout();
    }

    public long getDelayForScheduler(String name) {
        return delayCfg(name).getDelayForScheduler();
    }

    public void setDelayForScheduler(String name, long delay) {
        delayCfg(name).setDelayForScheduler(Math.max(0L, delay));
    }

    public LocalDateTime getSchedulerToDelay(String name) {
        return delayCfg(name).getSchedulerToDelay();
    }

    public void setSchedulerToDelay(String name, LocalDateTime scheduler) {
        delayCfg(name).setSchedulerToDelay(scheduler);
    }

    public long getDefaultDelay(String name) {
        return delayCfg(name).getDefaultDelay();
    }

    public long calculateMinus10PercentDelay(String serviceName) {
        long timeout = delayCfg(serviceName).getTimeout();
        return (long) (timeout * 0.9);
    }

    public void applyMinus10PercentToAll() {
        for (StubService service : registry.getAll()) {
            DelayConfig cfg = service.getDelayConfig();
            Objects.requireNonNull(cfg, "DelayConfig must not be null for service: " + service.getName());
            long timeout = cfg.getTimeout();
            if (timeout > 0) {
                cfg.setCurrentDelay((long) (timeout * 0.9));
            }
        }
    }

    // --- helpers ---
    private DelayConfig delayCfg(String name) {
        Objects.requireNonNull(name, "service name must not be null");
        StubService s = registry.get(name); // бросит ServiceNotFoundException для неизвестного сервиса
        DelayConfig cfg = s.getDelayConfig();
        return Objects.requireNonNull(cfg, "DelayConfig must not be null for service: " + name);
    }
}
