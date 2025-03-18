package com.LT.restDummy.servises;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@NoArgsConstructor
public class Service {
    private final LocalDateTime DEFAULT_DATE = LocalDateTime.of(2000, 01, 01, 01, 01);

    private String name;
    //    Ключ - число для процентного распределения
    private Map<Integer, String> response = new ConcurrentHashMap<>();
    //    отсортированные значения распределения
    private List<Integer> thresholds;
    private String fullServiceFile;
    private boolean percentage = false;
    private boolean available = true;
    private LocalDateTime availabilityScheduler = DEFAULT_DATE;
    private Long timeout;
    private Long defaultDelay;
    private Long currentDelay;
    private LocalDateTime schedulerToDelay = DEFAULT_DATE;
    private Long DelayForScheduler;
    private String type;
    private String endpoint;
    private String systemName = "Не указана";
    private boolean ChangeableParam = false;
    private String changeableParamName;
    private String changeableParamValue;
    private Integer changeableParamResponse;
    private Integer changeableDefaultResponse;

    public Service(String name, Long delay, boolean available) {
        this.name = name;
        this.currentDelay = delay;
        this.available = available;
    }

    public Service(String name, Long delay, boolean available, String systemName) {
        this.name = name;
        this.currentDelay = delay;
        this.available = available;
        this.systemName = systemName;
    }
}