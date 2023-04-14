package com.LT.restDummy.helper;

import com.LT.restDummy.date.DateModule;
import com.LT.restDummy.exception.ServiceException;
import com.LT.restDummy.servises.ResponseDelay;
import com.LT.restDummy.servises.Service;
import com.LT.restDummy.servises.ServiceMapper;
import com.LT.restDummy.servises.ServiceValue;
import com.LT.restDummy.servises.dto.ServiceRequestDto;
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

/*
Класс помощник для работы с ответами сервисов
*/
@Slf4j
public class ResponseHelper {
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String DATA_FOR_RANDOM_STRING_NUMBER = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER;
    private static SecureRandom random = new SecureRandom();

    public static CompletableFuture<ResponseEntity<String>> returnResponse(String request, String serviceName,
                                                                           long delay,
                                                                           Boolean isAvailable) {
        log.info("REQUEST: " + request);

/*
         Если параметры заданы, то обновляем их
*/
        if (delay != 0) {
            ServiceValue.getInstance().setNewDelayToService(serviceName, delay);
        }
        if (isAvailable != null) {
            ServiceValue.getInstance().setAvailabilityToService(serviceName, isAvailable);
        }
/*
         Если сервис доступен, то возвращаем его
*/
        if (ServiceValue.getInstance().getAvailabilityByService(serviceName)) {
/*
            передаем параметры для задержки: секунды, закоррелированный ответ и сервис
*/
            return ResponseDelay.scheduleResponse(ServiceValue.getInstance().getDelayByService(serviceName),
                    responseCorrelate(request,
                            getResponseByPercent(ServiceValue.getInstance().getServiceByName(serviceName)),
                            ServiceValue.getInstance().getTypeByService(serviceName)),
                    serviceName, setHeader(serviceName));
        } else throw new ServiceException("Сервис временно недоступен. Включите заглушку");
    }

    public static HttpHeaders setHeader(String serviceName) {
        HttpHeaders responseHeaders = new HttpHeaders();

        if (ServiceValue.getInstance().getTypeByService(serviceName).equalsIgnoreCase("json")) {
            responseHeaders.add("Content-Type", "application/json");
        } else responseHeaders.add("Content-Type", "application/xml");
        return responseHeaders;
    }

    /*
            Сортирует пороговые значения ответов по возрастанию, если рандомное число попадает в порог то отправляем ответ закрепленный за порогом
    */
    public static String getResponseByPercent(Service service) {
        int rand = 1 + (int) (Math.random() * 100);
        if (service.isPercentage()) {
            int startNumThreshold = 0;
            for (Integer endNumThreshold : service.getThresholds()) {
                if (rand > startNumThreshold && rand <= endNumThreshold)
                    return service.getResponse().get(endNumThreshold);
                else startNumThreshold = endNumThreshold;
            }
        }
        return service.getResponse().get(-1);
    }

    public static String parameterCorrelate(String request, String param, String type) {
        request = request.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        param = param.toLowerCase(Locale.ROOT);
        switch (type.toLowerCase(Locale.ROOT)) {
            case "xml":
                return StringUtils.substringBetween(request, "<" + param + ">", "</" + param + ">");
            case "json":
                return StringUtils.substringBetween(request, "\"" + param + "\":\"", "\"");
            default:
                return "null";
        }
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
/*
       собираем все параметры, необходимые к замене
*/
        Matcher matcher = Pattern.compile("__([a-zA-Z0-9<>]+)__").matcher(response);
        ArrayList<String> params = new ArrayList<>();
        while (matcher.find()) {
            params.add(matcher.group(1));
        }

        Pattern patternResponse;
        for (String param : params) {
/*
            Заменяем найденную подстроку на значение из запроса или текущее время
*/
            if (param.equalsIgnoreCase("rqtm") || param.equalsIgnoreCase("rstm")) {
                response = StringUtils.replace(response, "__" + param + "__", DateModule.get_date_now());
            } else if (param.toLowerCase(Locale.ROOT).contains("rndnumchar")) {
                int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                response = StringUtils.replace(response, "__" + param + "__", randomNumberAndChar(num));
            } else if (param.toLowerCase(Locale.ROOT).contains("rndnum")) {
                int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                response = StringUtils.replace(response, "__" + param + "__", randomNumber(num));
            } else if (param.toLowerCase(Locale.ROOT).contains("rndchar")) {
                int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                response = StringUtils.replace(response, "__" + param + "__", randomChar(num));
            }
/*
            Собираем подстроку которую нужно будет заменить в ответе. Пример: __RqUID__
*/
            switch (type) {
                case "xml":
                    patternResponse = Pattern.compile("<" + param + ">(__[a-zA-Z0-9]*__)<");
                    break;
                case "json":
                default:
                    patternResponse = Pattern.compile("\"" + param + "\":\"(__[a-zA-Z0-9]*__)\"");
                    break;
            }
            Matcher matcherResponse = patternResponse.matcher(response);
            while (matcherResponse.find()) {
                response = StringUtils.replace(response, matcherResponse.group(1), parameterCorrelate(request, param, type));
            }
        }
        return response;
    }


    public static JSONObject getServices() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", true);
        jsonObject.put("services", ServiceValue.getInstance().getServicesArray().
                stream().map(ServiceMapper::serviceToDto)
                .collect(Collectors.toList()));
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