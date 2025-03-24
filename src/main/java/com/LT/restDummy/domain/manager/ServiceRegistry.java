package com.LT.restDummy.domain.manager;

import com.LT.restDummy.exception.ServiceNotFoundException;
import com.LT.restDummy.domain.model.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {

    private final Map<String, Service> services = new ConcurrentHashMap<>();

    public void register(String name, Service service) {
        services.put(name, service);
    }

    public void registerAll(Map<String, Service> input) {
        services.putAll(input);
    }

    public Service get(String name) {
        Service service = services.get(name);
        if (service == null) {
            throw new ServiceNotFoundException(name);
        }
        return service;
    }

    public Collection<Service> getAll() {
        return services.values();
    }

    public List<String> getAllNames() {
        return new ArrayList<>(services.keySet());
    }
}