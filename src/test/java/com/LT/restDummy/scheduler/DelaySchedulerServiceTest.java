package com.LT.restDummy.scheduler;

import com.LT.restDummy.domain.delay.DelayConfig;
import com.LT.restDummy.domain.manager.ServiceDelayManager;
import com.LT.restDummy.domain.manager.ServiceRegistry;
import com.LT.restDummy.domain.model.StubService;
import com.LT.restDummy.service.ServiceValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static com.LT.restDummy.date.DateUtils.nowTruncatedToMinutes;
import static org.mockito.Mockito.*;

public class DelaySchedulerServiceTest {

    private ServiceValue serviceValue;
    private ServiceRegistry registry;
    private ServiceDelayManager delayManager;
    private DelaySchedulerService schedulerService;

    @BeforeEach
    public void setup() {
        registry = mock(ServiceRegistry.class);
        delayManager = mock(ServiceDelayManager.class);
        serviceValue = mock(ServiceValue.class);

        when(serviceValue.registry()).thenReturn(registry);
        when(serviceValue.delay()).thenReturn(delayManager);

        schedulerService = new DelaySchedulerService(serviceValue);
    }

    @Test
    public void testScheduledDelayApplied() {
        StubService stubService = mock(StubService.class);
        DelayConfig delayConfig = mock(DelayConfig.class);

        String serviceName = "stub-api";
        LocalDateTime now = nowTruncatedToMinutes();
        when(registry.getAll()).thenReturn(Collections.singletonList(stubService));
        when(stubService.getName()).thenReturn(serviceName);
        when(stubService.getDelayConfig()).thenReturn(delayConfig);
        when(delayConfig.getSchedulerToDelay()).thenReturn(now);

        when(delayManager.getDelayForScheduler(serviceName)).thenReturn(3000L);

        schedulerService.checkAndScheduleDelay();

        verify(delayManager).setDelay(serviceName, 3000L);
    }
}
