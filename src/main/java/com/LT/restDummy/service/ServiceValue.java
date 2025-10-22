package com.LT.restDummy.service;

import com.LT.restDummy.domain.manager.ServiceAvailabilityManager;
import com.LT.restDummy.domain.manager.ServiceDelayManager;
import com.LT.restDummy.domain.manager.ServiceRegistry;
import com.LT.restDummy.domain.model.StubService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class ServiceValue {

    private final ServiceRegistry registry;
    private final ServiceAvailabilityManager availability;
    private final ServiceDelayManager delay;

    // Мост к spring-бину (для совместимости со старым кодом)
    private static volatile ServiceValue INSTANCE;

    public ServiceValue() {
        this.registry = new ServiceRegistry();
        this.availability = new ServiceAvailabilityManager(registry);
        this.delay = new ServiceDelayManager(registry);
    }

    @PostConstruct
    void initStaticBridge() {
        INSTANCE = this;
    }

    /**
     * Временный мост для старого кода, который обращается к статическому синглтону.
     * Работает после старта Spring-контекста.
     */
    @Deprecated
    public static ServiceValue getInstance() {
        return Objects.requireNonNull(
                INSTANCE,
                "ServiceValue ещё не инициализирован: Spring context не поднят"
        );
    }

    public ServiceValue initialize(HashMap<String, StubService> services) {
        registry.registerAll(services);
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

    public void updateService(StubService updatingService) {
        delay.setDelay(updatingService.getName(), updatingService.getDelayConfig().getCurrentDelay());
        availability.setAvailable(updatingService.getName(), updatingService.isAvailable());
    }

    public String getTypeByService(String serviceName) {
        return registry.get(serviceName).getType();
    }

    public String getSystemNameByService(String serviceName) {
        return registry.get(serviceName).getSystemName();
    }

    public List<String> getServicesName() {
        return registry.getAllNames();
    }

    public static long calculateMinus10PercentDelay(long timeout) {
        return (long) (timeout * 0.9);
    }
}
