package com.LT.restDummy.service;

import com.LT.restDummy.domain.manager.ServiceAvailabilityManager;
import com.LT.restDummy.domain.manager.ServiceDelayManager;
import com.LT.restDummy.domain.manager.ServiceRegistry;
import com.LT.restDummy.domain.model.StubService;
import com.LT.restDummy.domain.response.ResponseResolver;
import com.LT.restDummy.domain.response.ResponseType;
import com.LT.restDummy.domain.response.StubResponse;
import com.LT.restDummy.exception.ServiceException;
import com.LT.restDummy.helper.ResponseCorrelatorService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResponseHandlerServiceTest {

    @Mock private ServiceValue serviceValue;
    @Mock private ResponseCorrelatorService correlatorService;
    @InjectMocks private ResponseHandlerService handlerService;

    @Mock private ServiceDelayManager delayManager;
    @Mock private ServiceAvailabilityManager availabilityManager;
    @Mock private ServiceRegistry registry;

    private final StubService stubService = new StubService();
    private final StubResponse stubResponse = new StubResponse("{\"result\": \"ok\"}");

    private MockedStatic<ResponseResolver> mockedStatic;

    @BeforeEach
    void setup() {
        lenient().when(serviceValue.delay()).thenReturn(delayManager);
        lenient().when(serviceValue.availability()).thenReturn(availabilityManager);
        lenient().when(serviceValue.registry()).thenReturn(registry);

        stubService.setType("json");
        stubService.setResponseType(ResponseType.DEFAULT);

        mockedStatic = mockStatic(ResponseResolver.class);
        mockedStatic.when(() -> ResponseResolver.resolve(any(), any())).thenReturn(stubResponse);
    }

    @AfterEach
    void tearDown() {
        mockedStatic.close();
    }

    @Test
    void shouldReturnResponse_WhenServiceAvailable() throws Exception {
        when(registry.get("testService")).thenReturn(stubService);
        when(availabilityManager.isAvailable("testService")).thenReturn(true);
        when(correlatorService.correlate(anyString(), anyString(), anyString()))
                .thenReturn("{\"result\": \"ok\"}");

        CompletableFuture<ResponseEntity<String>> future = handlerService.handle(
                "{\"key\":\"value\"}", "testService", 0L, null
        );

        ResponseEntity<String> response = future.get();
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("result"));
    }

    @Test
    void shouldThrowException_WhenServiceUnavailable() {
        when(registry.get("testService")).thenReturn(stubService); // 👈 ЭТО ВАЖНО!

        ServiceException ex = assertThrows(ServiceException.class, () ->
                handlerService.handle("{}", "testService", 0L, null)
        );

        assertEquals("Сервис временно недоступен. Включите заглушку", ex.getMessage());
    }

    @Test
    void shouldApplyDelayAndAvailabilityFlags() {
        when(registry.get("testService")).thenReturn(stubService);
        when(availabilityManager.isAvailable("testService")).thenReturn(true);
        when(correlatorService.correlate(anyString(), anyString(), anyString()))
                .thenReturn("OK");

        handlerService.handle("{}", "testService", 123L, false);

        verify(delayManager).setDelay("testService", 123L);
        verify(availabilityManager).setAvailable("testService", false);
    }

    @Test
    void shouldNotApplyDelayOrAvailability_WhenNullPassed() throws Exception {
        when(registry.get("testService")).thenReturn(stubService);
        when(availabilityManager.isAvailable("testService")).thenReturn(true);
        when(correlatorService.correlate(anyString(), anyString(), anyString()))
                .thenReturn("OK");

        handlerService.handle("{}", "testService", 0L, null);

        verify(delayManager, never()).setDelay(anyString(), anyLong());
        verify(availabilityManager, never()).setAvailable(anyString(), anyBoolean());
    }
    @Test
    void shouldThrowException_WhenServiceNotFound() {
        when(registry.get("unknownService")).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () ->
                handlerService.handle("{}", "unknownService", 0L, null)
        );

        assertTrue(ex.getMessage().contains("не найден"));
    }
    @Test
    void shouldThrowException_WhenCorrelationFails() {
        when(registry.get("testService")).thenReturn(stubService);
        when(availabilityManager.isAvailable("testService")).thenReturn(true);
        when(correlatorService.correlate(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Ошибка корреляции"));

        assertThrows(RuntimeException.class, () ->
                handlerService.handle("{}", "testService", 0L, null).join()
        );
    }
    @Test
    void shouldHandleEmptyRequestGracefully() throws Exception {
        when(registry.get("testService")).thenReturn(stubService);
        when(availabilityManager.isAvailable("testService")).thenReturn(true);
        when(correlatorService.correlate(eq(""), anyString(), anyString()))
                .thenReturn("OK");

        CompletableFuture<ResponseEntity<String>> future = handlerService.handle(
                "", "testService", 0L, null
        );

        assertEquals(200, future.get().getStatusCodeValue());
    }

}
