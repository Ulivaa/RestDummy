package com.LT.restDummy.service;

import com.LT.restDummy.domain.manager.ServiceAvailabilityManager;
import com.LT.restDummy.domain.manager.ServiceDelayManager;
import com.LT.restDummy.domain.manager.ServiceRegistry;
import com.LT.restDummy.domain.model.StubService;

import java.util.HashMap;
import java.util.List;

public class ServiceValue {

    private final ServiceRegistry registry = new ServiceRegistry();
    private final ServiceAvailabilityManager availability = new ServiceAvailabilityManager(registry);
    private final ServiceDelayManager delay = new ServiceDelayManager(registry);

    private static final ServiceValue INSTANCE = new ServiceValue();

    public static ServiceValue getInstance() {
        return INSTANCE;
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
