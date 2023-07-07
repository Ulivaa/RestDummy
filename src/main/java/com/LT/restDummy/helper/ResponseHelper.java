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
    private static final String secondResponseFindClaimById = "{\n" +
            "    \"status\": {\n" +
            "        \"severity\": \"COMPLETED\",\n" +
            "        \"code\": 0\n" +
            "    },\n" +
            "    \"claim\": {\n" +
            "        \"idbl\": 10012305000042438,\n" +
            "        \"type\": \"UfsIncreaseCardCreditLimit\",\n" +
            "        \"state\": \"SUCCESS\",\n" +
            "        \"initiator\": {\n" +
            "            \"sourceSystem\": \"urn:sbrfsystems:99-ufs\"\n" +
            "        },\n" +
            "        \"client\": {\n" +
            "            \"epkID\": \"__epkID__\"\n" +
            "        },\n" +
            "        \"data\": {\n" +
            "            \"operUID\": \"__operUID__\",\n" +
            "            \"systemName\": \"CREDIT_CARDS_LIMIT_INC_MB\",\n" +
            "            \"subSystemCode\": \"CREDIT_CARDS_LIMIT_INC_MB\",\n" +
            "            \"application\": {\n" +
            "                \"srcObjID\": \"00p3_0000000000001733431\",\n" +
            "                \"appDate\": \"2023-04-22\",\n" +
            "                \"unit\": \"99903801164\",\n" +
            "                \"channel\": \"16\",\n" +
            "                \"channelCBRegAApprove\": \"3\"\n" +
            "            },\n" +
            "            \"product\": {\n" +
            "                \"type\": \"3\",\n" +
            "                \"code\": \"50\",\n" +
            "                \"subProductCode\": \"911\",\n" +
            "                \"amount\": 200000,\n" +
            "                \"contractLoanAmount\": 510000,\n" +
            "                \"cardNumber\": \"220220F2A43E7809\",\n" +
            "                \"contractNumber\": \"99ТКПР23042100605803\",\n" +
            "                \"cardStatus\": \"+\",\n" +
            "                \"contractStatus\": \"+\",\n" +
            "                \"interestRate\": 23.9,\n" +
            "                \"preAmount\": 0\n" +
            "            },\n" +
            "            \"applicant\": {\n" +
            "                \"clientGroup\": \"x\",\n" +
            "                \"idEPK\": \"1786879448000710812\",\n" +
            "                \"appGroup\": \"x\",\n" +
            "                \"citizenship\": \"RUSSIA\",\n" +
            "                \"emailAddr\": \"FOO@FOO.RU\"\n" +
            "            },\n" +
            "            \"personInfo\": {\n" +
            "                \"lastName\": \"Шестаков\",\n" +
            "                \"firstName\": \"Изот\",\n" +
            "                \"middleName\": \"Филиппович\",\n" +
            "                \"sex\": \"1\",\n" +
            "                \"birthday\": \"1983-02-02\",\n" +
            "                \"birthPlace\": \"Сбер-Аист\"\n" +
            "            },\n" +
            "            \"phones\": [{\n" +
            "                    \"type\": \"1\",\n" +
            "                    \"countryPrefix\": \"9\",\n" +
            "                    \"prefix\": \"984\",\n" +
            "                    \"number\": \"750103\"\n" +
            "                }, {\n" +
            "                    \"type\": \"1\",\n" +
            "                    \"countryPrefix\": \"7\",\n" +
            "                    \"prefix\": \"991\",\n" +
            "                    \"number\": \"3578989\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"residenceEqualFlag\": true,\n" +
            "            \"address\": [{\n" +
            "                    \"manualInputFlag\": false,\n" +
            "                    \"addrType\": \"1\",\n" +
            "                    \"postalCode\": \"111677\",\n" +
            "                    \"regionCode\": \"0077\",\n" +
            "                    \"cityType\": \"301\",\n" +
            "                    \"city\": \"МОСКВА\",\n" +
            "                    \"streetType\": \"529\",\n" +
            "                    \"street\": \"НЕДОРУБОВА\",\n" +
            "                    \"houseNum\": \"23\",\n" +
            "                    \"unitNum\": \"9\"\n" +
            "                }, {\n" +
            "                    \"manualInputFlag\": false,\n" +
            "                    \"addrType\": \"2\",\n" +
            "                    \"postalCode\": \"111677\",\n" +
            "                    \"regionCode\": \"0077\",\n" +
            "                    \"cityType\": \"301\",\n" +
            "                    \"city\": \"МОСКВА\",\n" +
            "                    \"streetType\": \"529\",\n" +
            "                    \"street\": \"НЕДОРУБОВА\",\n" +
            "                    \"houseNum\": \"23\",\n" +
            "                    \"unitNum\": \"9\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"identityCard\": {\n" +
            "                \"idType\": \"21\",\n" +
            "                \"idSeries\": \"XXXX\",\n" +
            "                \"idNum\": \"944080\",\n" +
            "                \"issuedBy\": \"ОВД Соколиная гора\",\n" +
            "                \"issuedCode\": \"309-010\",\n" +
            "                \"issueDt\": \"2023-03-08\",\n" +
            "                \"prevIDInfoFlag\": false\n" +
            "            },\n" +
            "            \"income\": {\n" +
            "                \"basicIncome6M\": 200000\n" +
            "            },\n" +
            "            \"addData\": {\n" +
            "                \"cbReqApprovalFlag\": true,\n" +
            "                \"cbSendApprovalFlag\": true,\n" +
            "                \"pfrReqApprovalFlag\": false,\n" +
            "                \"consentOPSS\": true,\n" +
            "                \"signingDate\": \"2023-04-21\"\n" +
            "            },\n" +
            "            \"campaigningOfferInfo\": {\n" +
            "                \"channel\": \"MOB\",\n" +
            "                \"rType\": \"R400\",\n" +
            "                \"rDate\": \"2023-04-22T16:57:06.792Z\",\n" +
            "                \"rValue\": \"710000\"\n" +
            "            },\n" +
            "            \"approval\": {\n" +
            "                \"declineDate\": \"2023-04-23T00:00\",\n" +
            "                \"creditCardLimit\": 800000,\n" +
            "                \"thresholdPTI\": 700000\n" +
            "            },\n" +
            "            \"status\": {\n" +
            "                \"statusDesc\": \"Принято положительное решение\",\n" +
            "                \"declineCodeForInfo\": \"00\",\n" +
            "                \"applicationNumber\": \"4343530540\",\n" +
            "                \"declineDateForInfo\": \"2023-04-22\",\n" +
            "               \"declineReasonCode\": \"P001\",\n" +
            "                \"statusCode\": 14\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            " ";

    public static CompletableFuture<ResponseEntity<String>> returnResponse(String request, String serviceName,
                                                                           long delay,
                                                                           Boolean isAvailable) {
        log.info("REQUEST: " + request);

/**
 Если параметры заданы, то обновляем их
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
            if (serviceName.equals("findClaimById") && request.contains("10012305000042438")) {
                return ResponseDelay.scheduleResponse(ServiceValue.getInstance().getDelayByService(serviceName),
                        responseCorrelate(request,
                                secondResponseFindClaimById,
                                ServiceValue.getInstance().getTypeByService(serviceName)),
                        serviceName, setHeader(serviceName));
            } else {
                return ResponseDelay.scheduleResponse(ServiceValue.getInstance().getDelayByService(serviceName),
                        responseCorrelate(request,
                                getResponseByPercent(ServiceValue.getInstance().getServiceByName(serviceName)),
                                ServiceValue.getInstance    ().getTypeByService(serviceName)),
                        serviceName, setHeader(serviceName));
            }
        } else throw new ServiceException("Сервис временно недоступен. Включите заглушку");
    }

    public static HttpHeaders setHeader(String serviceName) {
        HttpHeaders responseHeaders = new HttpHeaders();

        if (ServiceValue.getInstance().getTypeByService(serviceName).equalsIgnoreCase("json")) {
            responseHeaders.add("Content-Type", "application/json");
        } else responseHeaders.add("Content-Type", "application/xml");
        return responseHeaders;
    }

    /**
     * Сортирует пороговые значения ответов по возрастанию, если рандомное число попадает в порог то отправляем ответ закрепленный за порогом
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
 собираем все параметры, необходимые к замене
 */

        Matcher matcher = Pattern.compile("__([a-zA-Z0-9<>]+)__").matcher(response);
        ArrayList<String> params = new ArrayList<>();
        while (matcher.find()) {
            params.add(matcher.group(1));
        }

        Pattern patternResponse;
        for (String param : params) {
/**
 Заменяем найденную подстроку на значение из запроса или текущее время
 */
            if (param.equalsIgnoreCase("rqtm") || param.equalsIgnoreCase("rstm")) {
                response = StringUtils.replace(response, "__" + param + "__", DateModule.get_date_now());
            } else if (param.toLowerCase(Locale.ROOT).contains("getnewrquid")) {
                int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                response = StringUtils.replace(response, "__" + param + "__", randomRqUID(num));
            }else if (param.toLowerCase(Locale.ROOT).contains("rndnumchar")) {
                int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                response = StringUtils.replace(response, "__" + param + "__", randomNumberAndChar(num));
            } else if (param.toLowerCase(Locale.ROOT).contains("rndnum")) {
                int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                response = StringUtils.replace(response, "__" + param + "__", randomNumber(num));
            } else if (param.toLowerCase(Locale.ROOT).contains("rndchar")) {
                int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                response = StringUtils.replace(response, "__" + param + "__", randomChar(num));
            }
/**
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