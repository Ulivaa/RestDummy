package com.LT.restDummy.domain.response;

import com.LT.restDummy.domain.manager.OccurrenceTracker;
import com.LT.restDummy.domain.model.StubService;
import com.LT.restDummy.util.JsonXmlParamExtractor;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class ResponseResolver {

    public static StubResponse resolve(StubService service, String request) {
        return resolve(service, request, null);
    }

    public static StubResponse resolve(StubService service, String request, OccurrenceTracker occurrenceTracker) {
        if (service == null) {
            return new StubResponse("Не задан сервис для выбора ответа");
        }
        final List<StubResponse> responses = service.getResponses();
        if (responses == null || responses.isEmpty()) {
            return new StubResponse("Ответ не найден (пустой список)");
        }

        final ResponseType mode = service.getResponseType();
        final String safeRequest = (request != null) ? request : "";

        if (mode == null || mode == ResponseType.DEFAULT) {
            // Возвращаем первый ненулевой ответ
            for (StubResponse r : responses) {
                if (r != null) return r;
            }
            return new StubResponse("Ответ не найден (все элементы пустые)");
        }

        switch (mode) {
            case THRESHOLD: {
                // Случайный выбор по «весам» (используем только положительные веса)
                final int rand = ThreadLocalRandom.current().nextInt(100); // 0..99
                int cumulative = 0;
                for (StubResponse r : responses) {
                    if (r == null) continue;
                    Integer w = r.getKey(); // key = вес
                    if (w == null || w <= 0) continue;
                    cumulative += w;
                    if (rand < cumulative) {
                        return r;
                    }
                }
                // Не попали ни в один диапазон — поведение прежнее: заглушка
                break;
            }

            case PARAM_BASED: {
                // Сначала ищем точное совпадение paramValue
                for (StubResponse r : responses) {
                    if (r == null || r.getType() != ResponseType.PARAM_BASED) continue;

                    String paramName = r.getParamName();
                    if (paramName == null || paramName.trim().isEmpty()) continue;

                    String expectedValue = r.getParamValue();
                    String actualValue = JsonXmlParamExtractor.extract(safeRequest, paramName, service.getType());

                    if (expectedValue != null && expectedValue.equals(actualValue)) {
                        return r;
                    }
                }
                // Fallback: ответ без paramValue (как было)
                for (StubResponse r : responses) {
                    if (r != null && r.getType() == ResponseType.PARAM_BASED && r.getParamValue() == null) {
                        return r;
                    }
                }
                break;
            }

            case OCCURRENCE_BASED: {
                if (occurrenceTracker == null || responses.isEmpty()) {
                    // Fallback: возвращаем первый ответ если tracker недоступен
                    return responses.get(0);
                }

                // Получаем параметры из первого ответа (все ответы имеют одинаковые настройки)
                StubResponse firstResponse = responses.get(0);
                if (firstResponse == null || firstResponse.getParamName() == null) {
                    return responses.get(0);
                }

                String keyParamName = firstResponse.getParamName();
                int switchAtOccurrence = firstResponse.getKey() != null ? firstResponse.getKey() : 2;

                // Извлекаем значение ключа из запроса
                String keyValue = JsonXmlParamExtractor.extract(safeRequest, keyParamName, service.getType());
                if (keyValue == null || keyValue.trim().isEmpty()) {
                    // Если не удалось извлечь ключ, возвращаем первый ответ
                    return responses.get(0);
                }

                // Инкрементируем счетчик вхождений для этого ключа
                String serviceName = service.getName();
                int currentOccurrence = occurrenceTracker.incrementAndGet(serviceName, keyValue);

                // Планируем очистку ключа, если настроено время очистки
                Long cleanupTimeMs = service.getOccurrenceCleanupTimeMs();
                if (cleanupTimeMs != null && cleanupTimeMs > 0) {
                    occurrenceTracker.scheduleKeyCleanup(serviceName, keyValue, cleanupTimeMs);
                }

                // Определяем какой ответ вернуть на основе номера вхождения
                int responseIndex;
                if (currentOccurrence < switchAtOccurrence) {
                    // До порога переключения - возвращаем первый ответ (индекс 0)
                    responseIndex = 0;
                } else {
                    // После порога - возвращаем второй ответ (индекс 1), если он есть
                    responseIndex = Math.min(1, responses.size() - 1);
                }

                return responses.get(responseIndex);
            }

            default:
                // неизвестный тип — поведение как «ничего не подошло»
                break;
        }

        return new StubResponse("Не удалось определить подходящий ответ");
    }
}
