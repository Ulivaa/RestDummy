package com.LT.restDummy.domain.model;

import com.LT.restDummy.domain.delay.DelayConfig;
import com.LT.restDummy.domain.response.ResponseType;
import com.LT.restDummy.domain.response.StubResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Service {
    public static final LocalDateTime DEFAULT_DATE = LocalDateTime.of(2000, 1, 1, 1, 1);

    private String name;
    private String fullServiceFile;
    private boolean available = true;
    private LocalDateTime availabilityScheduler = DEFAULT_DATE;

    private DelayConfig delayConfig;

    private String type;
    private String endpoint;
    private String systemName = "Не указана";

    // 🔁 Новый способ хранения ответов
    private List<StubResponse> responses;
    private ResponseType responseType;

    public Service(String name, Long defaultDelay, Long timeout, boolean available) {
        this.name = name;
        this.delayConfig = new DelayConfig(defaultDelay, timeout);
        this.available = available;
    }

    public Service(String name, Long defaultDelay, Long timeout, boolean available, String systemName) {
        this(name, defaultDelay, timeout, available);
        this.systemName = systemName;
    }
}
