package com.LT.restDummy.scheduler;

import com.LT.restDummy.domain.model.Service;
import com.LT.restDummy.service.ServiceValue;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Класс каждые 60с делает проверку текущего времени и времени задержки,
 * если оно совпадает - выставляется задержка или выключается сервис
 */
@Component
public class Scheduler {
    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    ServiceValue serviceValue;

    public Scheduler(ServiceValue serviceValue) {
        this.serviceValue = serviceValue;
    }

    @SneakyThrows
    @Scheduled(fixedRate = 60000)// как часто проверка(1 мин)
    public void schedulingAvailability() {
        boolean isScheduledAvailability = false;
        LocalDateTime now = LocalDateTime.parse(LocalDateTime.now().format(DATE_TIME_FORMATTER), DATE_TIME_FORMATTER);
        HashMap<String, Boolean> servicesStop = new HashMap<>();
/**
 Проверка на соответствие текущего времени и времени остановки сервиса, если хоть один соответствует,
 то он останавливается и время шедулится на 10минут
 */
        for (Service service : serviceValue.registry().getAll()) {
            if (service.getAvailabilityScheduler().isEqual(now)) {
                servicesStop.put(service.getName(), true);
                isScheduledAvailability = true;
            } else {
                servicesStop.put(service.getName(), false);
            }
        }
        for (String service : servicesStop.keySet()) {
            if (servicesStop.get(service)) {
                serviceValue.availability().setAvailable(service, false);
            }
        }
        if (isScheduledAvailability) {
            Thread.sleep(600000); // - 10 мин
            for (String service : servicesStop.keySet()) {
                if (servicesStop.get(service)) {
                    serviceValue.availability().setAvailable(service, true);
                }
            }
        }
    }

    @SneakyThrows
    @Scheduled(fixedRate = 60000) // как часто проверка(1 мин)
    public void schedulingDelay() {
        boolean isScheduledDelay = false;
        LocalDateTime now = LocalDateTime.parse(LocalDateTime.now().format(DATE_TIME_FORMATTER), DATE_TIME_FORMATTER);
        HashMap<String, Boolean> servicesDelay = new HashMap<>();

/**
 Проверка на соответствие текущего времени и времени остановки сервиса, если хоть один соответствует,
 то он останавливается и время шедулится на 10минут
 */
        for (Service service : serviceValue.registry().getAll()) {
            if (service.getDelayConfig().getSchedulerToDelay().isEqual(now)) {
                servicesDelay.put(service.getName(), true);
                isScheduledDelay = true;
            } else {
                servicesDelay.put(service.getName(), false);
            }
        }

        for (String service : servicesDelay.keySet()) {
            if (servicesDelay.get(service)) {
                serviceValue.delay().setDelay(service, ServiceValue.getInstance().delay().getDelayForScheduler(service));
            }
        }
        if (isScheduledDelay) {
            Thread.sleep(600000); // - 10 мин
            for (String service : servicesDelay.keySet()) {
                if (servicesDelay.get(service)) {
                    serviceValue.delay().setDelay(service, ServiceValue.getInstance().delay().getDefaultDelay(service));
                }
            }
        }

    }
}