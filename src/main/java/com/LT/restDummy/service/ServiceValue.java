package com.LT.restDummy.service;

import com.LT.restDummy.domain.manager.ServiceAvailabilityManager;
import com.LT.restDummy.domain.manager.ServiceDelayManager;
import com.LT.restDummy.domain.manager.ServiceRegistry;
import com.LT.restDummy.domain.model.StubService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Централизованное хранилище конфигурации сервисов (реестр + доступность + задержки).
 * Используется ТОЛЬКО как Spring-бин (конструкторная инъекция). Никакой статики и никаких new внутри.
 */
@Service
public class ServiceValue {

    private final ServiceRegistry registry;
    private final ServiceAvailabilityManager availability;
    private final ServiceDelayManager delay;

    // ВАЖНО: зависимости приходят извне (через Spring), а не создаются внутри.
    public ServiceValue(ServiceRegistry registry,
                        ServiceAvailabilityManager availability,
                        ServiceDelayManager delay) {
        this.registry = registry;
        this.availability = availability;
        this.delay = delay;
    }

    /** Инициализация реестра пачкой сервисов (например, при старте приложения). */
    public ServiceValue initialize(Map<String, StubService> services) {
        if (services != null && !services.isEmpty()) {
            registry.registerAll(services);
        }
        return this;
    }

    public ServiceRegistry registry() {
        return registry;
    }

    public ServiceAvailabilityManager availability() {
        return availability;
    }

    public ServiceDelayManager delay() {
        return delay;
    }

    /** Обновляет runtime-настройки сервиса (delay/availability) из переданного объекта. */
    public void updateService(StubService updatingService) {
        if (updatingService == null || updatingService.getName() == null) return;

        // null-safe: delayConfig может быть не задан
        long newDelay = 0L;
        if (updatingService.getDelayConfig() != null) {
            newDelay = updatingService.getDelayConfig().getCurrentDelay();
        }
        delay.setDelay(updatingService.getName(), newDelay);
        availability.setAvailable(updatingService.getName(), updatingService.isAvailable());
    }

    public String getTypeByService(String serviceName) {
        StubService s = registry.get(serviceName);
        return (s != null) ? s.getType() : null;
    }

    public String getSystemNameByService(String serviceName) {
        StubService s = registry.get(serviceName);
        return (s != null) ? s.getSystemName() : null;
    }

    public List<String> getServicesName() {
        return registry.getAllNames();
    }

    /** Утилита: 90% от таймаута (оставляем статической — это чистый helper без состояния). */
    public static long calculateMinus10PercentDelay(long timeout) {
        return (long) (timeout * 0.9);
    }
}
