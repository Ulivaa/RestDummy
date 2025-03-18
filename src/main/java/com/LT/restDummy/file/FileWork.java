package com.LT.restDummy.file;

import com.LT.restDummy.servises.Service;
import com.LT.restDummy.servises.ServiceValue;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Класс для работы с файлами и данными из файлов
 */
@Slf4j
public class FileWork {

    @SneakyThrows
    public static void fullFile(String fileName, String content) {
        File directory = new File("services");
        File file = new File(directory, fileName);
        byte[] strToBytes = content.getBytes();
        Files.write(strToBytes, file);
    }

    /**
     * Вынимаем параметры для конкретного сервиса из properties, преобразуем в мапу параметров сервиса для дальнейшей работы
     */
    public static Service getService(String name, String content) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("servicesParams.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        HashMap<String, String> params = new HashMap<>();
        properties.entrySet().stream().forEach(k -> params.put(k.getKey().toString().replace(name + ".", ""), k.getValue().toString()));

        return getService(name, content, params);
    }

    /**
     * Формируем сервис из переданных параметров
     */
    public static Service getService(String name, String content, HashMap<String, String> params) {
        Service service = new Service();
        Map<Integer, String> map = new ConcurrentHashMap<>();

        if (content.isEmpty()) {
            map.put(-1, "Текст сервиса не найден");
        } else {
            service.setFullServiceFile(content);
            Matcher matcherContent = Pattern.compile("-###-([\\s\\S]+?)-###-").matcher(content);
            List<String> responses = new ArrayList<>();
            List<Integer> threshold = new ArrayList<>();

            if (params.get("threshold") != null) {
                threshold = Arrays.stream(params.get("threshold")
                                .replace("[", "")
                                .replace("]", "")
                                .split(","))
                        .map(Integer::parseInt).collect(Collectors.toCollection(ArrayList::new));
            }
            if (!threshold.isEmpty()) {
                while (matcherContent.find()) {
                    responses.add(matcherContent.group(1));
                }
                if (responses.size() == 1) {
                    map.put(-1, responses.get(0));
                } else if (responses.size() > 1) {
                    service.setPercentage(true);
                    for (int i = 0; i < responses.size(); i++) {
                        map.put(threshold.get(i), responses.get(i).replaceAll(": \"", ":\""));
                    }
                } else {
                    map.put(-1, "не нашлось подходящих совпадений регулярки, убедитесь в правильности заполнения файла заглушки.");
                }
            } else if (params.get("param.name") != null && !params.get("param.name").isEmpty()
                    && params.get("param.value") != null && !params.get("param.value").isEmpty()
                    && params.get("param.responseNum") != null && !params.get("param.responseNum").isEmpty()) {
                while (matcherContent.find()) {
                    responses.add(matcherContent.group(1));
                }
                if (responses.size() > 1) {
                    service.setChangeableParam(true);
                    for (int i = 0; i < responses.size(); i++) {
//
//                        просто номер ответа
                        map.put(i + 1, responses.get(i).replaceAll(": \"", ":\""));
                    }
                }
            } else {
                map.put(-1, content);
            }
        }
        service.setResponse(map);
        service.setType(params.get("type"));
        service.setName(name);
        service.setDefaultDelay(Long.valueOf(params.get("delay")));
        service.setCurrentDelay(Long.valueOf(params.get("delay")));
        service.setTimeout(Long.valueOf(params.get("timeout")));
        service.setSystemName(params.getOrDefault("systemName", "Не указана"));
        service.setDelayForScheduler(ServiceValue.calculateMinus10PercentDelay(service.getTimeout()));
        service.setEndpoint(params.get("endpoint"));
        if (service.isPercentage()) {
            service.setThresholds(service.getResponse().keySet().stream().sorted().collect(Collectors.toList()));
        }
        if (service.isChangeableParam()) {
            service.setChangeableParamName(params.get("param.name"));
            service.setChangeableParamValue(params.get("param.value"));
            service.setChangeableParamResponse(Integer.valueOf(params.get("param.responseNum")));
            if (Integer.valueOf(params.get("param.responseNum")) == 1) {
                service.setChangeableDefaultResponse(2);
            } else {
                service.setChangeableDefaultResponse(1);
            }
        }
        return service;
    }

    public static List<String> getListFilesForFolder(final File folder) {
        if (!folder.exists()) {
            folder.mkdir();
        }
        List<String> arrayList = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getListFilesForFolder(fileEntry);
            } else {
                arrayList.add(fileEntry.getName());
            }
        }
        return arrayList;
    }

    //TODO переписать чтобы на вход подавался файл + тесты

    /**
     * Вытаскиваем сабсистем или создаем файл-уведомление
     */
    public static Properties getInfluxProperty() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("properties"));
        } catch (IOException e) {
            log.error("Вы не заполнили файл properties с параметром subsystem=VASHA_SUBSYSTEM");
        }
        return properties;
    }

    //TODO разделить + тесты
    public static void updateFilesServices(String name, String content, String params) {
        Pattern pattern = Pattern.compile("(.+)=(.+)");
        HashMap<String, String> map = new HashMap<>();
        Matcher matcher = pattern.matcher(params);
        while (matcher.find()) {
            map.put(matcher.group(1), matcher.group(2));
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("servicesParams.properties"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String fileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        for (String param : map.keySet()) {
            if (fileContent.contains(param)) {
                Pattern patternContent = Pattern.compile(param + "=(.+)");
                Matcher matcherContent = patternContent.matcher(fileContent);
                while (matcherContent.find()) {
                    fileContent = fileContent.replace(param + "=" + matcherContent.group(1), param + "=" + map.get(param));
                }
            } else {
                fileContent = fileContent.concat("\n" + param + "=" + map.get(param));
            }
        }
        if (!params.contains("endpoint")) {
            Pattern patternEndpoint = Pattern.compile(name + ".endpoint=(.+)");
            Matcher matcherEndpoint = patternEndpoint.matcher(fileContent);
            while (matcherEndpoint.find()) {
                fileContent = fileContent.replace(matcherEndpoint.group(1), "");
            }
        }
        if (!params.contains("threshold")) {
            Pattern patternThreshold = Pattern.compile("(" + name + ".threshold=.+)");
            Matcher matcherThreshold = patternThreshold.matcher(fileContent);
            while (matcherThreshold.find()) {
                fileContent = fileContent.replace(matcherThreshold.group(1), "");
            }
        }
        File file = new File("servicesParams.properties");
        byte[] strToBytes = fileContent.getBytes();
        try {
            Files.write(strToBytes, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fullFile(name, content);
    }
}