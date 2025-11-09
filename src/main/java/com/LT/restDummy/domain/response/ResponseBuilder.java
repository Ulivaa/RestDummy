package com.LT.restDummy.domain.response;

import com.LT.restDummy.domain.model.StubService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ResponseBuilder {

    /**
     * Строит список ответов для сервиса, поддерживая:
     * 1. Ответы из одного файла, разделенные -###-
     * 2. Ответы из нескольких файлов (serviceName-1, serviceName-2, ...)
     * 3. OCCURRENCE_BASED логику с настройками из params
     */
    public static List<StubResponse> build(StubService service, String content, Map<String, String> params) {
        List<StubResponse> result = new ArrayList<>();

        String serviceName = service.getName();
        
        // --- Парсим ответы: сначала пробуем множественные файлы, затем разделители в одном файле ---
        List<String> responses = loadResponses(serviceName, content);
        
        // --- Пустой контент — default-заглушка ---
        if (responses.isEmpty()) {
            result.add(new StubResponse("Текст сервиса не найден"));
            service.setResponseType(ResponseType.DEFAULT);
            return result;
        }

        // --- OCCURRENCE_BASED механизм (на основе вхождений по ключу) ---
        if (params.containsKey("occurrence.key.param") && 
            params.containsKey("occurrence.switchAt") && 
            responses.size() > 1) {
            
            String keyParamName = params.get("occurrence.key.param");
            int switchAtOccurrence = parseInt(params, "occurrence.switchAt", 2);
            long cleanupTimeMs = parseLong(params, "occurrence.cleanupTimeMs", 3600000L); // по умолчанию 1 час
            
            service.setResponseType(ResponseType.OCCURRENCE_BASED);
            service.setOccurrenceCleanupTimeMs(cleanupTimeMs); // Сохраняем время очистки
            
            // Сохраняем параметры для использования в ResponseResolver
            // key хранит switchAtOccurrence, paramName хранит keyParamName
            for (int i = 0; i < responses.size(); i++) {
                result.add(new StubResponse(switchAtOccurrence, responses.get(i), keyParamName, ResponseType.OCCURRENCE_BASED));
            }
            
            log.debug("Настроен OCCURRENCE_BASED для сервиса '{}': keyParam='{}', switchAt={}, cleanupTime={}ms", 
                     serviceName, keyParamName, switchAtOccurrence, cleanupTimeMs);
            
            return result;
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
                    result.add(new StubResponse(responseNum, responses.get(i), paramName, (String) null));
                }
            }

            return result;
        }

        // --- Fallback: обычный дефолтный ответ ---
        result.add(new StubResponse(content));
        service.setResponseType(ResponseType.DEFAULT);
        return result;
    }

    /**
     * Загружает ответы из файлов или из содержимого с разделителями.
     * Приоритет: сначала проверяет множественные файлы (serviceName-1, serviceName-2), 
     * затем парсит разделители -###- в одном файле.
     */
    private static List<String> loadResponses(String serviceName, String content) {
        List<String> responses = new ArrayList<>();
        
        // Пробуем загрузить из множественных файлов
        List<String> multiFileResponses = loadFromMultipleFiles(serviceName);
        if (!multiFileResponses.isEmpty()) {
            return multiFileResponses;
        }
        
        // Если множественных файлов нет, парсим разделители в одном файле
        if (content != null && !content.trim().isEmpty()) {
            Matcher matcher = Pattern.compile("-###-([\\s\\S]+?)-###-").matcher(content);
            while (matcher.find()) {
                responses.add(matcher.group(1).replaceAll(": \"", ":\""));
            }
            
            // Если не нашли разделителей, возвращаем весь контент как один ответ
            if (responses.isEmpty()) {
                responses.add(content);
            }
        }
        
        return responses;
    }

    /**
     * Загружает ответы из множественных файлов вида serviceName-1, serviceName-2, и т.д.
     */
    private static List<String> loadFromMultipleFiles(String serviceName) {
        List<String> responses = new ArrayList<>();
        File servicesDir = new File("services");
        
        if (!servicesDir.exists()) {
            return responses;
        }
        
        // Ищем файлы с именами serviceName-1, serviceName-2, ...
        int fileIndex = 1;
        while (true) {
            File file = new File(servicesDir, serviceName + "-" + fileIndex);
            if (!file.exists() || !file.isFile()) {
                break;
            }
            
            try {
                String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");
                if (!content.trim().isEmpty()) {
                    responses.add(content);
                    log.debug("Загружен файл ответа: {}", file.getName());
                }
                fileIndex++;
            } catch (IOException e) {
                log.warn("Ошибка при чтении файла {}: {}", file.getName(), e.getMessage());
                break;
            }
        }
        
        return responses;
    }

    private static int parseInt(Map<String, String> params, String key, int defaultValue) {
        if (params == null || !params.containsKey(key)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(params.get(key).trim());
        } catch (NumberFormatException e) {
            log.warn("Некорректное значение int для ключа '{}' = '{}', используем {}", 
                     key, params.get(key), defaultValue);
            return defaultValue;
        }
    }

    private static long parseLong(Map<String, String> params, String key, long defaultValue) {
        if (params == null || !params.containsKey(key)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(params.get(key).trim());
        } catch (NumberFormatException e) {
            log.warn("Некорректное значение long для ключа '{}' = '{}', используем {}", 
                     key, params.get(key), defaultValue);
            return defaultValue;
        }
    }
}
