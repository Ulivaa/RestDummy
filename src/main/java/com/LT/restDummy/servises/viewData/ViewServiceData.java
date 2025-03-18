package com.LT.restDummy.servises.viewData;

/**
 * Необходим для отображения через GUI
 */

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewServiceData {
    private String name;
    private Long currentDelay;
    private Long timeout;
    private Long delayForScheduler;
    private String schedulerDelay;
    private Boolean isAvailable;
    private String schedulerAvailability;
    private String systemName;

    public ViewServiceData(String name, Long currentDelay, Long timeout, Long delayForScheduler, String schedulerDelay, Boolean isAvailable, String schedulerAvailability, String systemName) {
        this.name = name;
        this.currentDelay = currentDelay;
        this.timeout = timeout;
        this.delayForScheduler = delayForScheduler;
        this.schedulerDelay = schedulerDelay;
        this.isAvailable = isAvailable;
        this.schedulerAvailability = schedulerAvailability;
        this.systemName = systemName;

    }

    //    Не удалять. Без него ломается HTML
    public ViewServiceData() {
        this.name = "rand";
        this.currentDelay = 0L;
        this.timeout = 0L;
        this.isAvailable = false;
    }

}