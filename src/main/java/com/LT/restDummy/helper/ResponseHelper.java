package com.LT.restDummy.helper;

import com.LT.restDummy.date.DateModule;
import com.LT.restDummy.exception.ServiceException;
import com.LT.restDummy.servises.Service;
import com.LT.restDummy.servises.ServiceMapper;
import com.LT.restDummy.servises.ServiceValue;
import com.LT.restDummy.servises.dto.ServiceRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Класс помощник для работы с ответами сервисов
 */
@Slf4j
public class ResponseHelper {
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_LOWER_RQUID = "abcdef";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String CHAR_UPPER_RQUID = CHAR_LOWER_RQUID.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String DATA_FOR_RANDOM_STRING_NUMBER = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static final String DATA_FOR_RQUID = CHAR_LOWER_RQUID + CHAR_UPPER_RQUID + NUMBER;
    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER;
    private static SecureRandom random = new SecureRandom();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static CompletableFuture<ResponseEntity<String>> returnResponse(String request, String serviceName,
                                                                           long delay,
                                                                           Boolean isAvailable) {
        log.info("REQUEST: " + request);

/**
 * Если параметры заданы, то обновляем их
 */
        if (delay != 0) {
            ServiceValue.getInstance().setNewDelayToService(serviceName, delay);
        }
        if (isAvailable != null) {
            ServiceValue.getInstance().setAvailabilityToService(serviceName, isAvailable);
        }
/**
 Если сервис доступен, то возвращаем его
 */
        if (ServiceValue.getInstance().getAvailabilityByService(serviceName)) {
/**
 передаем параметры для задержки: секунды, закоррелированный ответ и сервис
 */
            return ResponseDelay.scheduleResponse(ServiceValue.getInstance().getDelayByService(serviceName),
                    responseCorrelate(request,
                            getResponseFromBunch(ServiceValue.getInstance().getServiceByName(serviceName), request),
                            ServiceValue.getInstance().getTypeByService(serviceName)),
                    serviceName, getHeader(serviceName));
        } else throw new ServiceException("Сервис временно недоступен. Включите заглушку");
    }

    public static HttpHeaders getHeader(String serviceName) {
        HttpHeaders responseHeaders = new HttpHeaders();

        if (ServiceValue.getInstance().getTypeByService(serviceName).equalsIgnoreCase("json")) {
            responseHeaders.add("Content-Type", "application/json");
        } else responseHeaders.add("Content-Type", "application/xml");
        return responseHeaders;
    }

    /**
     * Сортирует пороговые значения ответов по возрастанию, если рандомное число попадает в порог то отправляем ответ закрепленный за порогом
     */
    public static String getResponseByPercent(Service service, int rand) {
        int startNumThreshold = 0;
        for (Integer endNumThreshold : service.getThresholds()) {
            if (rand > startNumThreshold && rand <= endNumThreshold) {
                return service.getResponse().get(endNumThreshold);
            } else startNumThreshold = endNumThreshold;
        }
        return "Какой-то параметр указан не верно. Перепроверьте.";
    }

    public static String getResponseFromBunch(Service service, String request) {
        if (service.isPercentage()) {
            return getResponseByPercent(service, getPercent());
        } else if (service.isChangeableParam()) {
            return getResponseByParam(service, request);
        }
        return service.getResponse().get(-1);
    }

    public static String getResponseByParam(Service service, String request) {
        if (parameterCorrelate(request, service.getChangeableParamName(), service.getType())
                .equals(service.getChangeableParamValue())) {
            return service.getResponse().get(service.getChangeableParamResponse());
        } else {
            return service.getResponse().get(service.getChangeableDefaultResponse());
        }
    }

    private static int getPercent() {
        return 1 + (int) (Math.random() * 100);
    }

    /**
     * Вынимает нужный параметр из запроса
     */
    public static String parameterCorrelate(String request, String param, String type) {
        request = request.replaceAll("\\s+", "");
        String value = null;
        switch (type.toLowerCase(Locale.ROOT)) {
            case "xml":
                value = StringUtils.substringBetween(request, "<" + param + ">", "</" + param + ">");
                break;
            case "json":
                JSONObject jsonObject = null;
                try {
                    jsonObject = objectMapper.readValue(request, JSONObject.class);
                } catch (JsonProcessingException e) {
                    log.error("Заглушка не может распарсить входящий запрос как json.");
                }
                if (jsonObject != null) {
                    if (jsonObject.containsKey(param)) {
                        Object obj = jsonObject.get(param);
                        return (obj instanceof List) ? String.valueOf(((List) obj).get(0)) : String.valueOf(obj);
                    } else if (!JsonPath.parse(request).read("$.." + param).toString().isEmpty()) {
                        List<String> arrayList = JsonPath.parse(request).read("$.." + param);
                        try {
                            if (arrayList.size() > 0) {
                                value = arrayList.get(0);
                            }
                        } catch (Exception e) {
                            try {
                                List<Long> secondArrayList = JsonPath.parse(request).read("$.." + param);
                                value = secondArrayList.get(0).toString();
                            } catch (Exception ex) {
                                List<Integer> secondArrayList = JsonPath.parse(request).read("$.." + param);
                                value = secondArrayList.get(0).toString();
                            }
                        }
                        break;
                    }
                } else {
                    return "Проверьте соответствие type и входящего запроса. Заглушка не может распарсить входящий запрос как json.";
                }
            default:
                return "У вас не указан type для сервиса или type не поддерживается";
        }
        if (value != null) {
            return value;
        } else {
            return "Значение не найдено в запросе";
        }
    }

    public static String randomRqUID(int length) {
        if (length < 1) throw new IllegalArgumentException();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(DATA_FOR_RQUID.length());
            char rndChar = DATA_FOR_RQUID.charAt(rndCharAt);
            sb.append(rndChar);
        }
        return sb.toString();
    }

    public static String randomNumberAndChar(int length) {
        if (length < 1) throw new IllegalArgumentException();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING_NUMBER.length());
            char rndChar = DATA_FOR_RANDOM_STRING_NUMBER.charAt(rndCharAt);
            sb.append(rndChar);
        }
        return sb.toString();

    }

    public static String randomNumber(int length) {
        if (length < 1) throw new IllegalArgumentException();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(NUMBER.length());
            char rndChar = NUMBER.charAt(rndCharAt);
            sb.append(rndChar);
        }
        return sb.toString();
    }

    public static String randomChar(int length) {
        if (length < 1) throw new IllegalArgumentException();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
            sb.append(rndChar);
        }
        log.info(sb.toString());
        return sb.toString();
    }

    public static String responseCorrelate(String request, String response, String type) {
/**
 собираем все параметры, необходимые к замене в response
 */
        response = searchParamWithValueInRequestForJson(response, request);

        Matcher matcher = Pattern.compile("__([a-zA-Z0-9<>_]+)__").matcher(response);
        List<String> params = new ArrayList<>();
        while (matcher.find()) {
            params.add(matcher.group(1));
        }

        Pattern patternResponse;
        for (String param : params) {
/**
 Заменяем найденную подстроку на рандомное значение или текущее время в ответе
 */
            if (param.equalsIgnoreCase("rqtm") || param.equalsIgnoreCase("rstm")) {
                response = StringUtils.replace(response, "__" + param + "__", DateModule.get_date_now());
            }
            if (param.toLowerCase(Locale.ROOT).contains("getnewrquid")) {
                int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                response = StringUtils.replace(response, "__" + param + "__", randomRqUID(num));
            }
            if (param.toLowerCase(Locale.ROOT).contains("rndnumchar")) {
                int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                response = StringUtils.replace(response, "__" + param + "__", randomNumberAndChar(num));
            }
            if (param.toLowerCase(Locale.ROOT).contains("rndnum")) {
                int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                response = StringUtils.replace(response, "__" + param + "__", randomNumber(num));
            }
            if (param.toLowerCase(Locale.ROOT).contains("rndchar")) {
                int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                response = StringUtils.replace(response, "__" + param + "__", randomChar(num));
            }
/**
 Собираем подстроку которую нужно будет заменить в ответе. Пример: __RqUID__
 */
            switch (type) {
                case "xml":
                    patternResponse = Pattern.compile("<" + param + ">(__[a-zA-Z0-9_]*__)<");
                    break;
                case "json":
                default:

//                    patternResponse = Pattern.compile("\"(?i).*" + param + ".*(__[a-zA-Z0-9_]*__)");
                    patternResponse = Pattern.compile("(__" + param + "__)");
                    break;
            }
            Matcher matcherResponse = patternResponse.matcher(response);
            while (matcherResponse.find()) {
                response = StringUtils.replace(response, matcherResponse.group(1), parameterCorrelate(request, param, type));
            }
        }
        return response;
    }

    public static String searchParamWithValueInRequestForJson(String response, String request) {
        Matcher matcher = Pattern.compile("___([a-zA-Z0-9<>_]+)___").matcher(response);
        Matcher matcher2 = Pattern.compile("_-_([a-zA-Z0-9<>_]+)_-_").matcher(response);
        List<String> params = new ArrayList<>();
        while (matcher.find()) {
            params.add(matcher.group(1));
        }
        List<String> params2 = new ArrayList<>();

        while (matcher2.find()) {
            params2.add(matcher2.group(1));
        }
        for (String param : params) {
            Pattern patternResponse = Pattern.compile("\"" + param + ".*(___[a-zA-Z0-9_]*___)");
            Matcher matcherResponse = patternResponse.matcher(response);
            while (matcherResponse.find()) {
                request = request.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
                param = param.toLowerCase(Locale.ROOT);
                response = StringUtils.replace(response,
                        matcherResponse.group(1),
                        StringUtils.substringBetween(request, "\"parameter\":\"" + param + "\",\"value\":\"", "\""));
            }
        }
        for (String param : params2) {
            Pattern patternResponse = Pattern.compile(".*(_-_[a-zA-Z0-9_]*_-_)");
            Matcher matcherResponse = patternResponse.matcher(response);
            while (matcherResponse.find()) {
                request = request.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
                param = param.toLowerCase(Locale.ROOT);
                response = StringUtils.replace(response,
                        matcherResponse.group(1),
                        StringUtils.substringBetween(request, "\"" + param + "\":{\"value\":\"", "\""));
            }
        }
        return response;
    }

    public static JSONObject getServices(String version) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", true);
        jsonObject.put("services", ServiceValue.getInstance().getServicesArray().
                stream().map(ServiceMapper::serviceToDto)
                .collect(Collectors.toList()));
        jsonObject.put("version", version);
        return jsonObject;
    }

    public static JSONObject editServices(List<ServiceRequestDto> services) {
        for (ServiceRequestDto dtoService : services) {
            ServiceValue.getInstance().updateService(ServiceMapper.dtoToService(dtoService));
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", true);
        return jsonObject;

    }
}