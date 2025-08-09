package com.LT.restDummy.service;

import com.LT.restDummy.domain.manager.ServiceRegistry;
import com.LT.restDummy.domain.model.StubService;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceManagementServiceTest {

    @Mock
    private ServiceValue serviceValue;

    @Mock
    private ServiceRegistry serviceRegistry;

    @InjectMocks
    private ServiceManagementService managementService;

    private StubService stubService;

    @BeforeEach
    void setUp() {
        stubService = new StubService();
        stubService.setName("test");
        stubService.setType("json");
        when(serviceValue.registry()).thenReturn(serviceRegistry);
    }

    @Test
    void shouldReturnServicesWithVersion() {
        when(serviceRegistry.getAll()).thenReturn(Collections.singletonList(stubService));

        JSONObject result = managementService.getServices("v1.2.3");

        assertTrue((Boolean) result.get("success"));
        assertEquals("v1.2.3", result.get("version"));
        assertNotNull(result.get("services"));
    }

    @Test
    void shouldEditServicesSuccessfully() {
        doNothing().when(serviceValue).updateService(any(StubService.class));

        JSONObject result = managementService.editServices(Arrays.asList(ServiceMapper.serviceToDto(stubService)));

        assertTrue((Boolean) result.get("success"));
        verify(serviceValue, times(1)).updateService(any(StubService.class));
    }

    @Test
    void shouldReturnEmptyListIfNoServicesConfigured() {
        when(serviceRegistry.getAll()).thenReturn(Collections.emptyList());

        JSONObject result = managementService.getServices("v0.0.0");

        assertTrue((Boolean) result.get("success"));
        assertEquals("v0.0.0", result.get("version"));
        List<?> services = (List<?>) result.get("services");
        assertTrue(services.isEmpty());
    }

    @Test
    void shouldHandleNullRegistryGracefully() {
        when(serviceRegistry.getAll()).thenReturn(null);

        JSONObject result = managementService.getServices("v2.0.0");

        assertTrue((Boolean) result.get("success"));
        assertEquals("v2.0.0", result.get("version"));
        Object services = result.get("services");
        assertNotNull(services);
        assertTrue(services instanceof List);
        assertTrue(((List<?>) services).isEmpty());
    }
}
