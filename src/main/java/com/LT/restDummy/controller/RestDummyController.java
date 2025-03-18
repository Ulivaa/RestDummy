package com.LT.restDummy.controller;

import com.LT.restDummy.helper.ResponseHelper;
import com.LT.restDummy.servises.dto.ServicesDto;
import com.LT.restDummy.victoria.VictoriaWriter;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
//import org.influxdb.InfluxDB;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

/**
 * Класс реализует ответ на rest вызов сервисов
 */

@Slf4j
@RestController
public class RestDummyController {

    //    public final InfluxDB influxDB;
    private String version;

    public RestDummyController(
//            InfluxDB influxDB,
            @Value("${application-version}") String version) {
//        this.influxDB = influxDB;
        this.version = version;
    }

    @RequestMapping("/services")
    public CompletableFuture<ResponseEntity<String>> getResponse(@RequestBody(required = false) String request,
                                                                 @RequestParam String service,
                                                                 @RequestParam(defaultValue = "0") Long delay,
                                                                 @RequestParam(required = false) Boolean isAvailable) {
        return ResponseHelper.returnResponse(request, service, delay, isAvailable);
    }

    @PostMapping("/customEndpoint/**")

    public CompletableFuture<ResponseEntity<String>> postResponseCustomEndpoint(@RequestBody(required = false) String request,
                                                                                @RequestParam(defaultValue = "0") Long delay,
                                                                                @RequestParam(required = false) Boolean isAvailable,
                                                                                HttpServletRequest httpServletRequest) {
        String path = httpServletRequest.getRequestURI().replaceAll("/customEndpoint", "");
        if (request == null) {
            request = "";
        }
        return ResponseHelper.returnResponse(request, path, delay, isAvailable);
    }
//TODO
//    "/customEndpoint/**"
//    public CompletableFuture<ResponseEntity<String>> postResponseCustomEndpoint(
//                                                                                defaultValue = "0" Long delay,
//                                                                                required = false Boolean isAvailable,
//                                                                                HttpServletRequest httpServletRequest) {
//        String path = httpServletRequest.getRequestURI().replaceAll("/customEndpoint", "");
//        return ResponseHelper.returnResponse("", path, delay, isAvailable);
//    }

    @GetMapping("/customEndpoint/**")
    public CompletableFuture<ResponseEntity<String>> getResponseCustomEndpoint(
            @RequestParam(defaultValue = "0") Long delay,
            @RequestParam(required = false) Boolean isAvailable,
            HttpServletRequest httpServletRequest) {
        String path = httpServletRequest.getRequestURI().replaceAll("/customEndpoint", "");
        return ResponseHelper.returnResponse("", path, delay, isAvailable);
    }

    @GetMapping("/getServices")
    public ResponseEntity<?> getServices() {
        return ResponseEntity.ok(ResponseHelper.getServices(version));
    }

    @PostMapping("/editServices")
    public ResponseEntity<?> editServices(@RequestBody String object) {
        Gson gson = new Gson();
        ServicesDto servicesDto = gson.fromJson(object, ServicesDto.class);
        return ResponseEntity.ok(ResponseHelper.editServices(servicesDto.getServices()));
    }

    @GetMapping("/version")
    public ResponseEntity<?> getVersion() {
        return ResponseEntity.ok("{version: \"" + version + "\"}");
    }
}