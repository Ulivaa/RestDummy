package com.LT.restDummy.controller;

import com.LT.restDummy.helper.ResponseHelper;
import com.LT.restDummy.servises.dto.ServicesDto;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

/**
Класс реализует ответ на rest вызов сервисов
*/
@Slf4j
@RestController
public class RestDummyController {

    public final InfluxDB influxDB;

    public RestDummyController(InfluxDB influxDB) {
        this.influxDB = influxDB;
    }


    @PostMapping("/services")
    public CompletableFuture<ResponseEntity<String>> getResponse(@RequestBody String request,
                                                                 @RequestParam String service,
                                                                 @RequestParam(defaultValue = "0") Long delay,
                                                                 @RequestParam(required = false) Boolean isAvailable) {
        return ResponseHelper.returnResponse(request, service, delay, isAvailable);
    }

    @PostMapping("/customEndpoint/**")
    public CompletableFuture<ResponseEntity<String>> getResponseCustomEndpoint(@RequestBody String request,
                                                                               @RequestParam(defaultValue = "0") Long delay,
                                                                               @RequestParam(required = false) Boolean isAvailable,
                                                                               HttpServletRequest httpServletRequest) {
        String path = httpServletRequest.getRequestURI().replaceAll("/customEndpoint", "");
        return ResponseHelper.returnResponse(request, path, delay, isAvailable);
    }

    @GetMapping("/getServices")
    public ResponseEntity<?> getServices() {
        return ResponseEntity.ok(ResponseHelper.getServices());
    }

    @PostMapping("/editServices")
    public ResponseEntity<?> editServices(@RequestBody String object) {
        Gson gson = new Gson();
        ServicesDto servicesDto = gson.fromJson(object, ServicesDto.class);
        return ResponseEntity.ok(ResponseHelper.editServices(servicesDto.getServices()));
    }
}
