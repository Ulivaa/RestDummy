package com.LT.restDummy.service;

import com.LT.restDummy.domain.model.StubService;
import com.LT.restDummy.domain.response.ResponseResolver;
import com.LT.restDummy.domain.response.StubResponse;
import com.LT.restDummy.exception.ServiceException;
import com.LT.restDummy.helper.ResponseCorrelatorService;
import com.LT.restDummy.helper.ResponseHeaderBuilder;
import com.LT.restDummy.helper.ResponseDelay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResponseHandlerService {

    private final ServiceValue serviceValue;
    private final ResponseCorrelatorService responseCorrelatorService;
    private final ResponseDelay responseDelay; // инжектим бин

    public CompletableFuture<ResponseEntity<String>> handle(
            String requestBody,
            String serviceName,
            long delay,
            Boolean isAvailable
    ) {
        final String safeRequestBody = (requestBody != null) ? requestBody : "";

        log.info("=== Обработка запроса к сервису: {} ===", serviceName);
        log.info("REQUEST BODY:\n{}", safeRequestBody);

        final StubService service = serviceValue.registry().get(serviceName);
        if (service == null) {
            throw new ServiceException("Сервис [" + serviceName + "] не найден в конфигурации");
        }

        // Переопределяем параметры по запросу (если заданы)
        if (delay != 0L) {
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

        final String type = service.getType();
        log.info("📦 Тип логики ответа: {}", service.getResponseType());

        final StubResponse resolved = ResponseResolver.resolve(service, safeRequestBody, serviceValue.occurrenceTracker());
        if (resolved == null) {
            throw new ServiceException("Не удалось определить ответ для сервиса [" + serviceName + "]");
        }

        log.info("✅ Выбран ответ: [type={}, key={}, paramName={}, paramValue={}]",
                resolved.getType(), resolved.getKey(), resolved.getParamName(), resolved.getParamValue());

        // NULL-SAFE: базовый контент никогда не null
        final String baseContent = Optional.ofNullable(resolved.getContent()).orElse("");

        // correlate может вернуть null — в этом случае используем baseContent
        // Исключения из correlate НЕ гасим, чтобы тест shouldThrowException_WhenCorrelationFails оставался валидным
        String correlatedBody = Optional.ofNullable(
                responseCorrelatorService.correlate(safeRequestBody, baseContent, type)
        ).orElse(baseContent);

        // подстрахуемся от null на всякий случай
        correlatedBody = Objects.toString(correlatedBody, "");

        final long effectiveDelay = serviceValue.delay().getDelay(serviceName);

        // используем инжектированный бин ResponseDelay
        return responseDelay.scheduleResponse(
                effectiveDelay,
                correlatedBody,
                serviceName,
                ResponseHeaderBuilder.build(type)
        );
    }
}
