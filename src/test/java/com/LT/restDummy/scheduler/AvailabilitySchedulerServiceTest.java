package com.LT.restDummy.scheduler;

import com.LT.restDummy.domain.manager.ServiceAvailabilityManager;
import com.LT.restDummy.domain.manager.ServiceRegistry;
import com.LT.restDummy.domain.model.StubService;
import com.LT.restDummy.service.ServiceValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static com.LT.restDummy.date.DateUtils.nowTruncatedToMinutes;
import static org.mockito.Mockito.*;

public class AvailabilitySchedulerServiceTest {

    private ServiceValue serviceValue;
    private ServiceRegistry registry;
    private ServiceAvailabilityManager availabilityManager;
    private AvailabilitySchedulerService schedulerService;

    @BeforeEach
    public void setup() {
        registry = mock(ServiceRegistry.class);
        availabilityManager = mock(ServiceAvailabilityManager.class);
        serviceValue = mock(ServiceValue.class);

        when(serviceValue.registry()).thenReturn(registry);
        when(serviceValue.availability()).thenReturn(availabilityManager);

        schedulerService = new AvailabilitySchedulerService(serviceValue);
    }

    @Test
    public void testScheduledAvailabilityDisablesService() {
        StubService stubService = mock(StubService.class);
        String serviceName = "test-service";

        LocalDateTime now = nowTruncatedToMinutes();
        when(registry.getAll()).thenReturn(Collections.singletonList(stubService));
        when(stubService.getName()).thenReturn(serviceName);
        when(stubService.getAvailabilityScheduler()).thenReturn(now);

        schedulerService.checkAndScheduleAvailability();

        verify(availabilityManager).setAvailable(serviceName, false);
    }
}
