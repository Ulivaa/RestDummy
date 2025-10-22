package com.LT.restDummy.controller;

import com.LT.restDummy.service.ResponseHandlerService;
import com.LT.restDummy.service.ServiceManagementService;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты только слоя MVC для RestDummyController.
 * Сервисы замоканы через @MockBean.
 */
@WebMvcTest(RestDummyController.class)
@TestPropertySource(properties = {
        "application-version=TEST_VER"
})
class RestDummyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResponseHandlerService responseHandlerService;

    @MockBean
    private ServiceManagementService serviceManagementService;

    // ===== /services

    @Test
    @DisplayName("POST /services — happy path, возвращаем тело ответа из сервиса")
    void postServices_shouldCallHandlerAndReturnBody() throws Exception {
        Mockito.when(responseHandlerService.handle(anyString(), eq("testService"), eq(5L), isNull()))
                .thenReturn(CompletableFuture.completedFuture(ResponseEntity.ok("OK")));

        mockMvc.perform(post("/services")
                        .param("service", "testService")
                        .param("delay", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"value\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        verify(responseHandlerService, times(1))
                .handle(eq("{\"key\":\"value\"}"), eq("testService"), eq(5L), isNull());
    }

    @Test
    @DisplayName("POST /services без параметра service — 400 Bad Request")
    void postServices_withoutServiceParam_shouldFail400() throws Exception {
        mockMvc.perform(post("/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        Mockito.verifyNoInteractions(responseHandlerService);
    }

    // ===== /customEndpoint/**

    @Test
    @DisplayName("POST /customEndpoint/foo/bar — путь прокидывается как имя/эндпоинт сервиса")
    void postCustomEndpoint_shouldProxyPathToHandler() throws Exception {
        Mockito.when(responseHandlerService.handle(anyString(), eq("/foo/bar"), eq(0L), isNull()))
                .thenReturn(CompletableFuture.completedFuture(ResponseEntity.ok("OK_CUSTOM_POST")));

        mockMvc.perform(post("/customEndpoint/foo/bar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"a\":1}"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK_CUSTOM_POST"));

        verify(responseHandlerService, times(1))
                .handle(eq("{\"a\":1}"), eq("/foo/bar"), eq(0L), isNull());
    }

    @Test
    @DisplayName("GET /customEndpoint/foo — GET без тела, delay=10")
    void getCustomEndpoint_shouldCallHandlerWithEmptyBody() throws Exception {
        Mockito.when(responseHandlerService.handle(eq(""), eq("/foo"), eq(10L), isNull()))
                .thenReturn(CompletableFuture.completedFuture(ResponseEntity.ok("OK_CUSTOM_GET")));

        mockMvc.perform(get("/customEndpoint/foo").param("delay", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK_CUSTOM_GET"));

        verify(responseHandlerService, times(1))
                .handle(eq(""), eq("/foo"), eq(10L), isNull());
    }

    // ===== /getServices

    @Test
    @DisplayName("GET /getServices — возвращает success и версию")
    void getServices_shouldReturnSuccessAndVersion() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("success", true);
        payload.put("version", "TEST_VER");
        payload.put("services", Collections.emptyList());
        Mockito.when(serviceManagementService.getServices(eq("TEST_VER"))).thenReturn(payload);

        mockMvc.perform(get("/getServices"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"success\":true")))
                .andExpect(content().string(containsString("\"version\":\"TEST_VER\"")));
    }

    // ===== /editServices

    @Test
    @DisplayName("POST /editServices — валидный JSON с services")
    void editServices_shouldAcceptValidJson() throws Exception {
        JSONObject ok = new JSONObject();
        ok.put("success", true);
        Mockito.when(serviceManagementService.editServices(ArgumentMatchers.anyList()))
                .thenReturn(ok);

        String body = "{\"services\":[{\"name\":\"s1\",\"delay\":100,\"timeout\":1000,\"available\":true,\"systemName\":\"sys\"}]}";

        mockMvc.perform(post("/editServices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"success\":true")));

        verify(serviceManagementService, times(1))
                .editServices(ArgumentMatchers.anyList());
    }

    @Test
    @DisplayName("POST /editServices — невалидный JSON → обрабатывается как success с пустым списком")
    void editServices_shouldHandleInvalidJsonGracefully() throws Exception {
        JSONObject ok = new JSONObject();
        ok.put("success", true);
        Mockito.when(serviceManagementService.editServices(eq(Collections.emptyList())))
                .thenReturn(ok);

        mockMvc.perform(post("/editServices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not a json"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"success\":true")));

        verify(serviceManagementService, times(1))
                .editServices(eq(Collections.emptyList()));
    }

    // ===== /version

    @Test
    @DisplayName("GET /version — отдаёт JSON со строкой версии")
    void version_shouldReturnConfiguredVersion() throws Exception {
        mockMvc.perform(get("/version"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("application/json")))
                .andExpect(content().json("{\"version\":\"TEST_VER\"}"));
    }
}
