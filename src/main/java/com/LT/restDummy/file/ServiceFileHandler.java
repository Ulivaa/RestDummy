package com.LT.restDummy.file;

import com.LT.restDummy.domain.model.StubService;
import com.LT.restDummy.service.ServiceValue;
import com.LT.restDummy.domain.delay.DelayConfig;
import com.LT.restDummy.domain.response.ResponseBuilder;
import com.LT.restDummy.domain.response.StubResponse;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Утилитный класс для работы с файлами и параметрами сервисов.
 */
@Slf4j
public class ServiceFileHandler {

    private static final String PARAMS_FILE = "servicesParams.properties";

    // ===========================
    // 📁 Работа с файлами заглушек
    // ===========================

    /**
     * Сохраняет содержимое сервиса (заглушку) в файл внутри папки `services`.
     *
     * @param fileName имя файла (чаще всего = имя сервиса)
     * @param content  содержимое заглушки
     */
    @SneakyThrows
    public static void fullFile(String fileName, String content) {
        File directory = new File("services");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, fileName);
        Files.write(content.getBytes(), file);
    }

    /**
     * Возвращает список всех файлов (не рекурсивно) в указанной папке.
     *
     * @param folder папка, из которой читаем
     * @return список имён файлов
     */
    public static List<String> getListFilesForFolder(File folder) {
        if (!folder.exists()) {
            folder.mkdir();
        }
        List<String> result = new ArrayList<>();
        collectFileNames(folder, result);
        return result;
    }

    private static void collectFileNames(File folder, List<String> result) {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isDirectory()) {
                collectFileNames(file, result);
            } else {
                result.add(file.getName());
            }
        }
    }

    // ===========================
    // 📦 Работа с параметрами сервиса
    // ===========================

    /**
     * Загружает параметры для конкретного сервиса из файла `servicesParams.properties` и формирует объект {@link StubService}.
     *
     * @param name    имя сервиса
     * @param content содержимое файла заглушки
     * @return сконфигурированный {@link StubService}
     */
    public static StubService getService(String name, String content) {
        Properties properties = readPropertiesFile(PARAMS_FILE);

        HashMap<String, String> params = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(name + ".")) {
                String shortKey = key.substring((name + ".").length());
                params.put(shortKey, entry.getValue().toString());
            }
        }

        return getService(name, content, params);
    }

    /**
     * Создаёт объект {@link StubService} на основе имени, содержимого заглушки и переданных параметров.
     *
     * @param name    имя сервиса
     * @param content содержимое файла заглушки
     * @param params  параметры сервиса, извлечённые из .properties
     * @return готовый {@link StubService}
     */
    public static StubService getService(String name, String content, HashMap<String, String> params) {
        StubService service = new StubService();
        service.setName(name);
        service.setFullServiceFile(content);

        // 📦 Ответы
        List<StubResponse> stubResponses = ResponseBuilder.build(service, content, params);
        service.setResponses(stubResponses);
        service.setResponseType(service.getResponseType()); // уже установлен в ResponseBuilder

        // 🌐 Общие параметры
        service.setType(params.get("type"));
        service.setSystemName(params.getOrDefault("systemName", "Не указана"));
        service.setEndpoint(params.get("endpoint"));

        // ⏱️ Задержки
        Long defaultDelay = Long.valueOf(params.get("delay"));
        Long timeout = Long.valueOf(params.get("timeout"));
        Long delayForScheduler = ServiceValue.calculateMinus10PercentDelay(timeout);

        DelayConfig delayConfig = new DelayConfig(defaultDelay, timeout);
        delayConfig.setDelayScheduler(DelayConfig.DEFAULT_DATE, delayForScheduler);
        service.setDelayConfig(delayConfig);

        // 🕒 Дата доступности
        service.setAvailabilityScheduler(StubService.DEFAULT_DATE);

        return service;
    }

    // ===========================
    // 🛠 Обновление servicesParams.properties
    // ===========================

    /**
     * Обновляет параметры сервиса в `servicesParams.properties` и записывает файл-заглушку.
     *
     * @param name       имя сервиса
     * @param content    содержимое заглушки
     * @param rawParams  строка с параметрами вида key=value\nkey2=value2
     */
    public static void updateFilesServices(String name, String content, String rawParams) {
        Map<String, String> updates = parseInlineParams(rawParams);
        Properties props = readPropertiesFile(PARAMS_FILE);

        updateProperties(props, updates);
        removeIfMissing(props, name, updates);
        saveServiceParamsFile(props);

        fullFile(name, content);
    }

    /**
     * Загружает свойства из указанного файла `.properties`.
     *
     * @param path путь до файла
     * @return {@link Properties} с параметрами
     */
    public static Properties readPropertiesFile(String path) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            properties.load(fis);
        } catch (IOException e) {
            log.error("Не удалось загрузить файл: {}", path, e);
        }
        return properties;
    }

    private static Map<String, String> parseInlineParams(String rawParams) {
        Map<String, String> result = new HashMap<>();
        Pattern pattern = Pattern.compile("(.+?)=(.+)");
        Matcher matcher = pattern.matcher(rawParams);
        while (matcher.find()) {
            result.put(matcher.group(1), matcher.group(2));
        }
        return result;
    }

    private static void updateProperties(Properties props, Map<String, String> updates) {
        updates.forEach(props::setProperty);
    }

    private static void removeIfMissing(Properties props, String serviceName, Map<String, String> updates) {
        if (!updates.containsKey(serviceName + ".endpoint")) {
            props.remove(serviceName + ".endpoint");
        }
        if (!updates.containsKey(serviceName + ".threshold")) {
            props.remove(serviceName + ".threshold");
        }
    }

    private static void saveServiceParamsFile(Properties props) {
        try (FileOutputStream out = new FileOutputStream(PARAMS_FILE)) {
            props.store(out, null);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить servicesParams.properties", e);
        }
    }
}
