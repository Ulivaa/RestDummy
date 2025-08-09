package com.LT.restDummy.scheduler;

import com.LT.restDummy.domain.model.StubService;
import com.LT.restDummy.service.ServiceValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.*;

import static com.LT.restDummy.date.DateUtils.nowTruncatedToMinutes;

@Slf4j
@Service
@RequiredArgsConstructor
public class DelaySchedulerService {

    private final ServiceValue serviceValue;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public void checkAndScheduleDelay() {
        LocalDateTime now = nowTruncatedToMinutes();
        for (StubService service : serviceValue.registry().getAll()) {
            if (service.getDelayConfig().getSchedulerToDelay().isEqual(now)) {
                log.info("⏱ Applying scheduled delay for {}", service.getName());
                long delayValue = serviceValue.delay().getDelayForScheduler(service.getName());
                serviceValue.delay().setDelay(service.getName(), delayValue);

                executor.schedule(() -> {
                    log.info("✅ Reverting delay for {}", service.getName());
                    serviceValue.delay().setDelay(service.getName(), serviceValue.delay().getDefaultDelay(service.getName()));
                }, 10, TimeUnit.MINUTES);
            }
        }
    }
}
