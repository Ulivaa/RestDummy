package com.LT.restDummy.domain.manager;

import com.LT.restDummy.exception.ServiceNotFoundException;
import com.LT.restDummy.domain.model.StubService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {

    private final Map<String, StubService> services = new ConcurrentHashMap<>();

    public void register(String name, StubService service) {
        services.put(name, service);
    }

    public void registerAll(Map<String, StubService> input) {
        services.putAll(input);
    }

    public StubService get(String name) {
        StubService service = services.get(name);
        if (service == null) {
            throw new ServiceNotFoundException(name);
        }
        return service;
    }

    public Collection<StubService> getAll() {
        return services.values();
    }

    public List<String> getAllNames() {
        return new ArrayList<>(services.keySet());
    }
}