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
public class AvailabilitySchedulerService {

    private final ServiceValue serviceValue;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public void checkAndScheduleAvailability() {
        LocalDateTime now = nowTruncatedToMinutes();
        for (StubService service : serviceValue.registry().getAll()) {
            if (service.getAvailabilityScheduler().isEqual(now)) {
                String name = service.getName();
                log.info("🔴 Disabling service {}", name);
                serviceValue.availability().setAvailable(name, false);

                executor.schedule(() -> {
                    log.info("🟢 Re-enabling service {}", name);
                    serviceValue.availability().setAvailable(name, true);
                }, 10, TimeUnit.MINUTES);
            }
        }
    }
}
