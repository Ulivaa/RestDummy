package com.LT.restDummy.domain.response;

import com.LT.restDummy.helper.ResponseHelper;
import com.LT.restDummy.domain.model.Service;

import java.util.List;
import java.util.Random;

public class ResponseResolver {

    private static final Random random = new Random();

    public static StubResponse resolve(Service service, String request) {
        List<StubResponse> responses = service.getResponses();

        if (responses == null || responses.isEmpty()) {
            return new StubResponse("Ответ не найден (пустой список)");
        }

        switch (service.getResponseType()) {

            case DEFAULT:
                return responses.get(0);

            case THRESHOLD:
                int rand = random.nextInt(100);
                int cumulative = 0;
                for (StubResponse r : responses) {
                    cumulative += r.getKey(); // key = вес
                    if (rand < cumulative) {
                        return r;
                    }
                }
                break;

            case PARAM_BASED:
                for (StubResponse r : responses) {
                    if (r.getType() != ResponseType.PARAM_BASED) continue;

                    String paramName = r.getParamName();
                    String expectedValue = r.getParamValue();
                    String actualValue = ResponseHelper.parameterCorrelate(request, paramName,service.getType());

                    if (expectedValue != null && expectedValue.equals(actualValue)) {
                        return r; // Совпадение параметра — возвращаем целевой ответ
                    }
                }

                // Не совпало — ищем fallback (где paramValue == null)
                for (StubResponse r : responses) {
                    if (r.getType() == ResponseType.PARAM_BASED && r.getParamValue() == null) {
                        return r;
                    }
                }

                break;
        }

        // Совсем ничего не подошло — возвращаем заглушку
        return new StubResponse("Не удалось определить подходящий ответ");
    }
}
