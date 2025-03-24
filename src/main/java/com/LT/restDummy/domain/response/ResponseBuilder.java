package com.LT.restDummy.domain.response;

import com.LT.restDummy.domain.model.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ResponseBuilder {

    public static List<StubResponse> build(Service service, String content, Map<String, String> params) {
        List<StubResponse> result = new ArrayList<>();

        // --- Пустой контент — default-заглушка ---
        if (content == null || content.trim().isEmpty()) {
            result.add(new StubResponse("Текст сервиса не найден"));
            service.setResponseType(ResponseType.DEFAULT);
            return result;
        }

        // --- Парсим ответы из файла по разделителям ---
        List<String> responses = new ArrayList<>();
        Matcher matcher = Pattern.compile("-###-([\\s\\S]+?)-###-").matcher(content);
        while (matcher.find()) {
            responses.add(matcher.group(1).replaceAll(": \"", ":\""));
        }

        // --- THRESHOLD-механизм (по весам) ---
        if (params.containsKey("threshold")) {
            List<Integer> rawWeights = Arrays.stream(params.get("threshold")
                            .replace("[", "")
                            .replace("]", "")
                            .split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            if (rawWeights.size() == responses.size()) {
                int total = rawWeights.stream().mapToInt(Integer::intValue).sum();
                if (total != 100) {
                    throw new IllegalArgumentException("Сумма значений threshold должна быть ровно 100, а не " + total);
                }

                service.setResponseType(ResponseType.THRESHOLD);
                for (int i = 0; i < responses.size(); i++) {
                    result.add(new StubResponse(rawWeights.get(i), responses.get(i)));
                }

                return result;
            }
        }

        // --- PARAM_BASED (если заданы параметры и более одного ответа) ---
        if (params.containsKey("param.name") &&
                params.containsKey("param.value") &&
                params.containsKey("param.responseNum") &&
                responses.size() > 1) {

            String paramName = params.get("param.name");
            String paramValue = params.get("param.value");
            int expectedResponseNum = Integer.parseInt(params.get("param.responseNum"));

            service.setResponseType(ResponseType.PARAM_BASED);

            for (int i = 0; i < responses.size(); i++) {
                int responseNum = i + 1;
                if (responseNum == expectedResponseNum) {
                    result.add(new StubResponse(responseNum, responses.get(i), paramName, paramValue));
                } else {
                    result.add(new StubResponse(responseNum, responses.get(i), paramName, null));
                }
            }

            return result;
        }

        // --- Fallback: обычный дефолтный ответ ---
        result.add(new StubResponse(content));
        service.setResponseType(ResponseType.DEFAULT);
        return result;
    }
}
