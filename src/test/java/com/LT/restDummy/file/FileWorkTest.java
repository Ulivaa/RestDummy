package com.LT.restDummy.file;

import com.LT.restDummy.servises.Service;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class FileWorkTest {
    private static String thresholdContent = "-###-\n" +
            "{\n" +
            "    \"status\": {\n" +
            "        \"statusCode\": 0,\n" +
            "        \"errorCode\": null,\n" +
            "        \"errorMessage\": null\n" +
            "        },\n" +
            "    \"loanList\": []\n" +
            "}\n" +
            "-###-\n" +
            "-###-\n" +
            "{\n" +
            "    \"status\": {\n" +
            "        \"statusCode\": 1,\n" +
            "        \"errorCode\": null,\n" +
            "        \"errorMessage\": null\n" +
            "        },\n" +
            "    \"loanList\": []\n" +
            "}\n" +
            "-###-\n" +
            "-###-\n" +
            "{\n" +
            "    \"status\": {\n" +
            "        \"statusCode\": 2,\n" +
            "        \"errorCode\": null,\n" +
            "        \"errorMessage\": null\n" +
            "        },\n" +
            "    \"loanList\": []\n" +
            "}\n" +
            "-###-";
    private static String simpleContent = "{\"status\": {\n" +
            "                \"statusCode\": 0,\n" +
            "                \"errorCode\": null,\n" +
            "                \"errorMessage\": null\n" +
            "            },\n" +
            "            \"loanList\": []\n" +
            "        }";

    @Test
    public void shouldGetServiceWithoutEndpointAndThreshold() {
        HashMap<String, String> params = new HashMap<>();
        params.put("type", "json");
        params.put("timeout", "3000");
        params.put("delay", "1000");
        Service service = FileWork.getService("service", simpleContent, params);
        assertEquals("service", service.getName());
        assertEquals(simpleContent, service.getFullServiceFile());
        assertEquals(Long.valueOf(3000), service.getTimeout());
        assertEquals(Long.valueOf(1000), service.getDefaultDelay());
        assertEquals(Long.valueOf(1000), service.getCurrentDelay());
        assertEquals(Long.valueOf(2700), service.getDelayForScheduler());
        assertEquals("json", service.getType());
        assertEquals(LocalDateTime.of(2000, 01, 01, 01, 01), service.getSchedulerToDelay());
        assertEquals(LocalDateTime.of(2000, 01, 01, 01, 01), service.getAvailabilityScheduler());
        assertNull(service.getEndpoint());
        assertNull(service.getThresholds());
        assertFalse(service.isPercentage());
        assertTrue(service.isAvailable());
    }

    @Test

    public void shouldGetServiceWithEndpointAndThreshold() {
        HashMap<String, String> params = new HashMap<>();
        params.put("type", "json");
        params.put("timeout", "3000");
        params.put("delay", "1000");
        params.put("endpoint", "/end/sss/a");
        params.put("threshold", "[45,60,100]");
        Service service = FileWork.getService("service", thresholdContent, params);
        assertEquals("service", service.getName());
        assertEquals(thresholdContent, service.getFullServiceFile());
        assertEquals(Long.valueOf(3000), service.getTimeout());
        assertEquals(Long.valueOf(1000), service.getDefaultDelay());
        assertEquals(Long.valueOf(1000), service.getCurrentDelay());
        assertEquals(Long.valueOf(2700), service.getDelayForScheduler());
        assertEquals("json", service.getType());
        assertEquals(LocalDateTime.of(2000, 01, 01, 01, 01), service.getSchedulerToDelay());
        assertEquals(LocalDateTime.of(2000, 01, 01, 01, 01), service.getAvailabilityScheduler());
        assertEquals("/end/sss/a", service.getEndpoint());
        assertEquals("[45, 60, 100]", service.getThresholds().toString());
        assertTrue(service.isPercentage());
        assertTrue(service.isAvailable());
    }
}