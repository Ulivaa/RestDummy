package com.LT.restDummy.servises;

import com.LT.restDummy.exception.IncorrectParameterException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceValue {
    private static final ConcurrentHashMap<String, Service> services = new ConcurrentHashMap<>();

    public static class ServiceValueNewHolder {
        static final ServiceValue HOLDER_INSTANCE = new ServiceValue();
    }

    /**
         необходимо для использования нестатической переменной в статическом методе
    */
    public static ServiceValue getInstance() {
        return ServiceValue.ServiceValueNewHolder.HOLDER_INSTANCE;
    }

    public ServiceValue initialize(HashMap<String, Service> responses) {
        services.putAll(responses);
        return this;
    }

    public String getTypeByService(String serviceName) {
        return services.get(serviceName).getType();
    }


    public String getFullFileByService(String serviceName) {
        return services.getOrDefault(serviceName, null).getFullServiceFile();
    }

    public ConcurrentHashMap<String, Service> getServices() {
        return services;
    }

    public Collection<Service> getServicesArray() {
        return services.values();
    }

    public Service getServiceByName(String serviceName) {
        return services.getOrDefault(serviceName, null);
    }

    public void setService(String serviceName, Service service) {
        services.put(serviceName, service);
    }


    public void updateService(Service updatingService) {
        Service service;
        for (Service serv: services.values()){
            if (serv.getName().equals(updatingService.getName())){
            service = serv;
            service.setCurrentDelay(updatingService.getCurrentDelay());
            service.setAvailable(updatingService.isAvailable());}
        }
    }

    /**
            AVAILABILITY
    */
    public boolean getAvailabilityByService(@NonNull String serviceName) {
        return services.get(serviceName).isAvailable();
    }

    public LocalDateTime getSchedulerToAvailabilityByService(@NonNull String serviceName) {
        return services.get(serviceName).getAvailabilityScheduler();
    }

    public void setAvailabilityToService(@NonNull String serviceName, @NonNull boolean availability) {
        services.get(serviceName).setAvailable(availability);
    }

    public void setAvailabilityToAllService(@NonNull boolean availability) {
        for (Service service : services.values()) {
            service.setAvailable(availability);
        }
    }

    public void setSchedulerForAvailabilityToService(@NonNull String serviceName, @NonNull LocalDateTime scheduler) {
        services.get(serviceName).setAvailabilityScheduler(scheduler);
    }


    /**
        DELAY
    */
    public long getDelayByService(@NonNull String serviceName) {
        return services.get(serviceName).getCurrentDelay();
    }

    public long getTimeoutByService(@NonNull String serviceName) {
        return services.get(serviceName).getTimeout();
    }

    public List<String> getServicesName() {
        return Collections.list(services.keys());
    }

    public Long getServiceDefaultDelay(@NonNull String serviceName) {
        return services.get(serviceName).getDefaultDelay();
    }

    public void setNewDelayToService(@NonNull String serviceName, @NonNull long valueOfDelay) {
        if (services.containsKey(serviceName)
        ) {
            services.get(serviceName).setCurrentDelay(valueOfDelay);
        } else {
            throw new IncorrectParameterException(serviceName);
        }
    }


    public LocalDateTime getSchedulerToDelayByService(@NonNull String serviceName) {
        return services.get(serviceName).getSchedulerToDelay();
    }

    /**
            возвращать задержку для шедуллера
    */
    public long getDelayForSchedulerByService(@NonNull String serviceName) {
        return services.get(serviceName).getDelayForScheduler();
    }


    /**
            выставлять задержку для шедуллера
    */
    public void setNewDelayToScheduler(@NonNull String serviceName, @NonNull long valueOfDelay) {
        if (services.containsKey(serviceName)
        ) {
            services.get(serviceName).setDelayForScheduler(valueOfDelay);
        } else {
            throw new IncorrectParameterException(serviceName);
        }
    }

    public void setSchedulerToDelay(@NonNull String serviceName, @NonNull LocalDateTime scheduler) {
        if (services.containsKey(serviceName)) {
            services.get(serviceName).setSchedulerToDelay(scheduler);
        } else
            log.error("Cant set new value for '{}', service not found", serviceName);
    }

    /**
            ставит задержку -10% от таймаута. Если таймаут не задан, то оставляет задержку как есть.
    */
    public void setMinus10PercentDelay() {
        for (Service service : services.values()) {
            if (service.getTimeout() > 0) {
                service.setCurrentDelay(calculateMinus10PercentDelay(service.getTimeout()));
            }
        }
    }

    public static long calculateMinus10PercentDelay(long timeout) {
        Double newDelay = timeout * 0.9;
        return newDelay.longValue();
    }


    public void setDefaultDelayForAllService() {
        for (Service service : services.values()) {
            service.setCurrentDelay(service.getDefaultDelay());
        }
    }

}