package com.LT.restDummy.helper;

import com.LT.restDummy.date.DateModule;
import com.LT.restDummy.util.JsonXmlParamExtractor;
import com.LT.restDummy.util.RandomUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ResponseCorrelatorService {
    public String correlate(String request, String response, String type) {
        response = searchCustomMarkers(response, request);

        Matcher matcher = Pattern.compile("__([a-zA-Z0-9<>_]+)__").matcher(response);
        List<String> params = new ArrayList<>();
        while (matcher.find()) {
            params.add(matcher.group(1));
        }

        for (String param : params) {
            String placeholder = "__" + param + "__";

            try {
                if (param.equalsIgnoreCase("rqtm") || param.equalsIgnoreCase("rstm")) {
                    response = StringUtils.replace(response, placeholder, DateModule.get_date_now());
                    continue;
                }

                if (param.toLowerCase().contains("getnewrquid")) {
                    int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                    response = StringUtils.replace(response, placeholder, RandomUtils.randomRqUID(num));
                    continue;
                }

                if (param.toLowerCase().contains("rndnumchar")) {
                    int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                    response = StringUtils.replace(response, placeholder, RandomUtils.randomNumberAndChar(num));
                    continue;
                }

                if (param.toLowerCase().contains("rndnum")) {
                    int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                    response = StringUtils.replace(response, placeholder, RandomUtils.randomNumber(num));
                    continue;
                }

                if (param.toLowerCase().contains("rndchar")) {
                    int num = Integer.parseInt(StringUtils.substringBetween(param, "<", ">"));
                    response = StringUtils.replace(response, placeholder, RandomUtils.randomChar(num));
                    continue;
                }
            } catch (NumberFormatException e) {
                log.warn("Некорректный формат параметра {} — пропускаем", param);
                continue;
            }

            Pattern patternResponse = Pattern.compile(Pattern.quote(placeholder));
            Matcher matcherResponse = patternResponse.matcher(response);
            while (matcherResponse.find()) {
                String value = JsonXmlParamExtractor.extract(request, param, type);
                response = StringUtils.replace(response, matcherResponse.group(0), value);
            }
        }

        return response;
    }

    /**
     * Замена ___param___ и _-_param_-_ в ответе на данные из запроса, сохраняя регистр
     */
    private String searchCustomMarkers(String response, String request) {
        Matcher matcher = Pattern.compile("___([a-zA-Z0-9<>_]+)___").matcher(response);
        Matcher matcher2 = Pattern.compile("_-_([a-zA-Z0-9<>_]+)_-_").matcher(response);

        List<String> params1 = new ArrayList<>();
        while (matcher.find()) params1.add(matcher.group(1));

        List<String> params2 = new ArrayList<>();
        while (matcher2.find()) params2.add(matcher2.group(1));

        for (String param : params1) {
            Pattern pattern = Pattern.compile("\"" + param + ".*(___[a-zA-Z0-9_]*___)");
            Matcher matcherResponse = pattern.matcher(response);
            while (matcherResponse.find()) {
                String raw = request.replaceAll("\\s+", "");
                String key = "\"parameter\":\"" + param + "\",\"value\":\"";
                String replacement = StringUtils.substringBetween(raw, key, "\"");
                if (replacement != null)
                    response = StringUtils.replace(response, matcherResponse.group(1), replacement);
            }
        }

        for (String param : params2) {
            Pattern pattern = Pattern.compile(".*(_-_[a-zA-Z0-9_]*_-_)");
            Matcher matcherResponse = pattern.matcher(response);
            while (matcherResponse.find()) {
                String raw = request.replaceAll("\\s+", "");
                String key = "\"" + param + "\":{\"value\":\"";
                String replacement = StringUtils.substringBetween(raw, key, "\"");
                if (replacement != null)
                    response = StringUtils.replace(response, matcherResponse.group(1), replacement);
            }
        }

        return response;
    }}