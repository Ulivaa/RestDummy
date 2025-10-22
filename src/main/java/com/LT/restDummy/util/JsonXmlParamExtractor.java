package com.LT.restDummy.util;

import com.jayway.jsonpath.JsonPath;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@UtilityClass
public class JsonXmlParamExtractor {

    /**
     * Универсальный метод извлечения значения по имени параметра.
     * Поведение сохранено: json/xml ветки и дефолтное сообщение при неизвестном type.
     */
    public static String extract(String request, String param, String type) {
        // прежняя логика: перед разбором вычищаем все пробелы
        final String normalizedRequest = (request != null) ? request.replaceAll("\\s+", "") : "";
        final String normalizedType = (type != null) ? type.trim().toLowerCase(Locale.ROOT) : "";

        switch (normalizedType) {
            case "xml":
                return extractFromXml(normalizedRequest, param);
            case "json":
                return extractFromJson(normalizedRequest, param);
            default:
                return "У вас не указан type для сервиса или type не поддерживается";
        }
    }

    private static String extractFromJson(String request, String param) {
        if (StringUtils.isBlank(request) || StringUtils.isBlank(param)) {
            log.warn("Пустой запрос или имя параметра");
            return null;
        }
        try {
            List<Object> values = JsonPath.parse(request).read("$.." + param);
            if (values == null || values.isEmpty()) {
                log.debug("Параметр '{}' не найден через JsonPath", param);
                return null;
            }

            Object value = values.get(0);
            if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                return String.valueOf(value);
            }

            // Если это вложенный объект, например {"abc": {"value": "YES"}}
            if (value instanceof Map) {
                Object nestedValue = ((Map<?, ?>) value).get("value");
                return nestedValue != null ? String.valueOf(nestedValue) : null;
            }

            return null;
        } catch (Exception e) {
            log.warn("Ошибка при извлечении параметра '{}' через JsonPath: {}", param, e.getMessage());
            return null;
        }
    }

    private static String extractFromXml(String request, String param) {
        if (StringUtils.isBlank(request) || StringUtils.isBlank(param)) {
            log.warn("Пустой запрос или имя параметра");
            return null;
        }
        try {
            InputSource source = new InputSource(new StringReader(request));
            XPath xpath = XPathFactory.newInstance().newXPath();
            String expression = String.format("//*[local-name()='%s']", param);
            // Возвращает строковое значение первого найденного узла
            return xpath.evaluate(expression, source);
        } catch (Exception e) { // ловим любые ошибки парсинга/выражения
            log.warn("⚠️ XPath не смог найти/распарсить элемент '{}': {}", param, e.getMessage());
            return null;
        }
    }
}
