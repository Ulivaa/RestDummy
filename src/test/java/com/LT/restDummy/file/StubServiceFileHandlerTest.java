package com.LT.restDummy.file;

import com.LT.restDummy.domain.model.StubService;
import com.LT.restDummy.domain.response.ResponseType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class StubServiceFileHandlerTest {
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
        StubService service = ServiceFileHandler.getService("service", simpleContent, params);
        assertEquals("service", service.getName());
        assertEquals(simpleContent, service.getFullServiceFile());
        assertEquals(Long.valueOf(3000), service.getDelayConfig().getTimeout());
        assertEquals(Long.valueOf(1000), service.getDelayConfig().getDefaultDelay());
        assertEquals(Long.valueOf(1000), service.getDelayConfig().getCurrentDelay());
        assertEquals(Long.valueOf(2700), service.getDelayConfig().getDelayForScheduler());
        assertEquals("json", service.getType());
        assertEquals(LocalDateTime.of(2000, 01, 01, 01, 01), service.getDelayConfig().getSchedulerToDelay());
        assertEquals(LocalDateTime.of(2000, 01, 01, 01, 01), service.getAvailabilityScheduler());
        assertNull(service.getEndpoint());
//        assertNull(service.getThresholds());
//        assertFalse(service.isPercentage());
        assertTrue(service.isAvailable());
    }

    @Test

    public void shouldGetServiceWithEndpointAndThreshold() {
        HashMap<String, String> params = new HashMap<>();
        params.put("type", "json");
        params.put("timeout", "3000");
        params.put("delay", "1000");
        params.put("endpoint", "/end/sss/a");
        params.put("threshold", "[45,15,40]");
        StubService service = ServiceFileHandler.getService("service", thresholdContent, params);
        assertEquals("service", service.getName());
        assertEquals(thresholdContent, service.getFullServiceFile());
        assertEquals(Long.valueOf(3000), service.getDelayConfig().getTimeout());
        assertEquals(Long.valueOf(1000), service.getDelayConfig().getDefaultDelay());
        assertEquals(Long.valueOf(1000), service.getDelayConfig().getCurrentDelay());
        assertEquals(Long.valueOf(2700), service.getDelayConfig().getDelayForScheduler());
        assertEquals("json", service.getType());
        assertEquals(LocalDateTime.of(2000, 01, 01, 01, 01), service.getDelayConfig().getSchedulerToDelay());
        assertEquals(LocalDateTime.of(2000, 01, 01, 01, 01), service.getAvailabilityScheduler());
        assertEquals("/end/sss/a", service.getEndpoint());
        assertEquals("45", service.getResponses().get(0).getKey().toString());
        assertEquals("15", service.getResponses().get(1).getKey().toString());
        assertEquals("40", service.getResponses().get(2).getKey().toString());
        assertTrue(service.getResponses().get(0).getType() == ResponseType.THRESHOLD);
        assertTrue(service.getResponses().get(1).getType() == ResponseType.THRESHOLD);
        assertTrue(service.getResponses().get(2).getType() == ResponseType.THRESHOLD);
        assertTrue(service.isAvailable());
    }
}