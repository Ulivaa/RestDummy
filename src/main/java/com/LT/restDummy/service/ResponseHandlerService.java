package com.LT.restDummy.service;

import com.LT.restDummy.domain.model.StubService;
import com.LT.restDummy.domain.response.ResponseResolver;
import com.LT.restDummy.domain.response.StubResponse;
import com.LT.restDummy.exception.ServiceException;
import com.LT.restDummy.helper.ResponseCorrelatorService;
import com.LT.restDummy.helper.ResponseDelay;
import com.LT.restDummy.helper.ResponseHeaderBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResponseHandlerService {

    private final ServiceValue serviceValue;
    private final ResponseCorrelatorService responseCorrelatorService;

    public CompletableFuture<ResponseEntity<String>> handle(
            String requestBody,
            String serviceName,
            long delay,
            Boolean isAvailable
    ) {
        log.info("=== Обработка запроса к сервису: {} ===", serviceName);
        log.info("REQUEST BODY:\n{}", requestBody);

        StubService service = serviceValue.registry().get(serviceName);
        if (service == null) {
            throw new ServiceException("Сервис [" + serviceName + "] не найден в конфигурации");
        }

        // Устанавливаем параметры
        if (delay != 0) {
            serviceValue.delay().setDelay(serviceName, delay);
            log.info("⏱ Установлена новая задержка: {} мс", delay);
        }

        if (isAvailable != null) {
            serviceValue.availability().setAvailable(serviceName, isAvailable);
            log.info("🔁 Доступность сервиса обновлена: {}", isAvailable);
        }

        if (!serviceValue.availability().isAvailable(serviceName)) {
            throw new ServiceException("Сервис временно недоступен. Включите заглушку");
        }

        String type = service.getType();

        log.info("📦 Тип логики ответа: {}", service.getResponseType());

        StubResponse response = ResponseResolver.resolve(service, requestBody);

        log.info("✅ Выбран ответ: [type={}, key={}, paramName={}, paramValue={}]",
                response.getType(),
                response.getKey(),
                response.getParamName(),
                response.getParamValue());

        String correlatedBody = responseCorrelatorService.correlate(requestBody, response.getContent(), type);

        return ResponseDelay.scheduleResponse(
                serviceValue.delay().getDelay(serviceName),
                correlatedBody,
                serviceName,
                ResponseHeaderBuilder.build(service.getType())
        );
    }
}
