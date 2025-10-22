package com.LT.restDummy.controller;

import com.LT.restDummy.domain.dto.ServicesDto;
import com.LT.restDummy.service.ResponseHandlerService;
import com.LT.restDummy.service.ServiceManagementService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * Класс реализует ответ на REST-вызовы сервисов
 */
@Slf4j
@RestController
public class RestDummyController {

    private final ResponseHandlerService responseHandlerService;
    private final ServiceManagementService serviceManagementService;
    private final String version;
    private final Gson gson = new Gson();

    public RestDummyController(ResponseHandlerService responseHandlerService,
                               ServiceManagementService serviceManagementService,
                               @Value("${application-version}") String version) {
        this.responseHandlerService = responseHandlerService;
        this.serviceManagementService = serviceManagementService;
        this.version = version;
    }

    /** Поддерживаем старое поведение: метод не фиксируем (GET/POST и т.п.). */
    @RequestMapping("/services")
    public ResponseEntity<String> getResponse(@RequestBody(required = false) String request,
                                              @RequestParam String service,
                                              @RequestParam(defaultValue = "0") Long delay,
                                              @RequestParam(required = false) Boolean isAvailable) {
        final String safeRequest = request != null ? request : "";
        final long safeDelay = delay != null ? delay : 0L;
        return responseHandlerService.handle(safeRequest, service, safeDelay, isAvailable).join();
    }

    @PostMapping("/customEndpoint/**")
    public ResponseEntity<String> postResponseCustomEndpoint(@RequestBody(required = false) String request,
                                                             @RequestParam(defaultValue = "0") Long delay,
                                                             @RequestParam(required = false) Boolean isAvailable,
                                                             HttpServletRequest httpServletRequest) {
        final String path = stripCustomPrefix(httpServletRequest.getRequestURI());
        final String safeRequest = request != null ? request : "";
        final long safeDelay = delay != null ? delay : 0L;
        return responseHandlerService.handle(safeRequest, path, safeDelay, isAvailable).join();
    }

    @GetMapping("/customEndpoint/**")
    public ResponseEntity<String> getResponseCustomEndpoint(@RequestParam(defaultValue = "0") Long delay,
                                                            @RequestParam(required = false) Boolean isAvailable,
                                                            HttpServletRequest httpServletRequest) {
        final String path = stripCustomPrefix(httpServletRequest.getRequestURI());
        final long safeDelay = delay != null ? delay : 0L;
        return responseHandlerService.handle("", path, safeDelay, isAvailable).join();
    }

    @GetMapping("/getServices")
    public ResponseEntity<?> getServices() {
        return ResponseEntity.ok(serviceManagementService.getServices(version));
    }

    @PostMapping("/editServices")
    public ResponseEntity<?> editServices(@RequestBody String body) {
        try {
            ServicesDto servicesDto = gson.fromJson(body, ServicesDto.class);
            List<?> list = servicesDto != null ? servicesDto.getServices() : null;
            if (list == null) {
                // Пустой список/некорректный payload — сохраняем старую семантику: success = true
                return ResponseEntity.ok(serviceManagementService.editServices(Collections.emptyList()));
            }
            return ResponseEntity.ok(serviceManagementService.editServices(servicesDto.getServices()));
        } catch (JsonSyntaxException ex) {
            log.warn("Некорректный JSON в /editServices: {}", ex.getMessage());
            // Сохраняем контракт: success = true
            return ResponseEntity.ok(serviceManagementService.editServices(Collections.emptyList()));
        }
    }

    @GetMapping("/version")
    public ResponseEntity<?> getVersion() {
        // Валидный JSON + правильный Content-Type
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"version\":\"" + version + "\"}");
    }

    // --- helpers ---
    private static String stripCustomPrefix(String uri) {
        if (uri == null) return "";
        final String prefix = "/customEndpoint";
        return uri.startsWith(prefix) ? uri.substring(prefix.length()) : uri;
    }
}
