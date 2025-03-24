package com.LT.restDummy.service.delay;

import com.LT.restDummy.domain.delay.DelayConfig;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class DelayConfigTest {

    private DelayConfig delayConfig;

    @Before
    public void setUp() {
        // default = 1000, timeout = 2000
        delayConfig = new DelayConfig(1000L, 2000L);
    }

    @Test
    public void testInitialValues() {
        assertEquals(1000L, delayConfig.getDefaultDelay().longValue());
        assertEquals(1000L, delayConfig.getCurrentDelay().longValue());
        assertEquals(2000L, delayConfig.getTimeout().longValue());
    }

    @Test
    public void testUpdateCurrentDelay() {
        delayConfig.setCurrentDelay(1500L);
        assertEquals(1500L, delayConfig.getCurrentDelay().longValue());
        assertEquals("Default delay must remain unchanged", 1000L, delayConfig.getDefaultDelay().longValue());
    }

    @Test
    public void testDelayForScheduler() {
        delayConfig.setDelayForScheduler(1200L);
        assertEquals(1200L, delayConfig.getDelayForScheduler().longValue());
    }

    @Test
    public void testSchedulerToDelay() {
        LocalDateTime now = LocalDateTime.now();
        delayConfig.setSchedulerToDelay(now);
        assertEquals(now, delayConfig.getSchedulerToDelay());
    }

    @Test
    public void testNoOverrideOfDefaultOnReassign() {
        delayConfig.setCurrentDelay(1800L);
        delayConfig.setDelayForScheduler(1500L);

        // Заново присваиваем current — default не должен измениться
        delayConfig.setCurrentDelay(800L);

        assertEquals(1000L, delayConfig.getDefaultDelay().longValue());
        assertEquals(800L, delayConfig.getCurrentDelay().longValue());
    }
}
