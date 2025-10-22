package com.LT.restDummy.domain.manager;

import com.LT.restDummy.exception.ServiceNotFoundException;
import com.LT.restDummy.domain.model.StubService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {

    private final Map<String, StubService> services = new ConcurrentHashMap<String, StubService>();

    public void register(String name, StubService service) {
        Objects.requireNonNull(name, "service name must not be null");
        Objects.requireNonNull(service, "service must not be null");
        services.put(name, service);
    }

    public void registerAll(Map<String, StubService> input) {
        if (input == null || input.isEmpty()) return;
        for (Map.Entry<String, StubService> e : input.entrySet()) {
            // reuse валидацию register(...)
            register(e.getKey(), e.getValue());
        }
    }

    public StubService get(String name) {
        Objects.requireNonNull(name, "service name must not be null");
        StubService service = services.get(name);
        if (service == null) {
            throw new ServiceNotFoundException(name);
        }
        return service;
    }

    public Collection<StubService> getAll() {
        // снапшот + unmodifiable, чтобы снаружи не правили коллекцию
        return Collections.unmodifiableList(new ArrayList<StubService>(services.values()));
    }

    public List<String> getAllNames() {
        return Collections.unmodifiableList(new ArrayList<String>(services.keySet()));
    }
}
