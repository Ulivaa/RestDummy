package com.LT.restDummy.domain.manager;

import com.LT.restDummy.domain.model.StubService;
import com.LT.restDummy.exception.ServiceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реестр заглушечных сервисов.
 * Потокобезопасен, предназначен для использования как Spring-бин (singleton).
 */
@Component
public class ServiceRegistry {

    private final Map<String, StubService> services = new ConcurrentHashMap<>();

    /** Зарегистрировать/переобновить сервис по имени. */
    public void register(String name, StubService service) {
        Objects.requireNonNull(name, "service name must not be null");
        Objects.requireNonNull(service, "service must not be null");
        services.put(name, service);
    }

    /** Массовая регистрация сервисов. Пропускает null/пустые коллекции. */
    public void registerAll(Map<String, StubService> input) {
        if (input == null || input.isEmpty()) return;
        for (Map.Entry<String, StubService> e : input.entrySet()) {
            register(e.getKey(), e.getValue());
        }
    }

    /** Получить сервис по имени или бросить ServiceNotFoundException. */
    public StubService get(String name) {
        Objects.requireNonNull(name, "service name must not be null");
        StubService service = services.get(name);
        if (service == null) {
            throw new ServiceNotFoundException(name);
        }
        return service;
    }

    /** Неподдерживаемая внешняя модификация коллекция-снимок всех сервисов. */
    public Collection<StubService> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(services.values()));
    }

    /** Неподдерживаемая внешняя модификация коллекция-снимок имен сервисов. */
    public List<String> getAllNames() {
        return Collections.unmodifiableList(new ArrayList<>(services.keySet()));
    }
}
