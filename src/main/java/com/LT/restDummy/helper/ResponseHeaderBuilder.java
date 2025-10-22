package com.LT.restDummy.helper;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class ResponseHeaderBuilder {

    private static final Set<String> HOP_BY_HOP = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
                    "connection",
                    "keep-alive",
                    "proxy-authenticate",
                    "proxy-authorization",
                    "te",
                    "trailer",
                    "transfer-encoding",
                    "upgrade"
            ))
    );

    /**
     * Базовый метод — оставляем поведение 1:1:
     * - если type == "json" (без учёта регистра) → Content-Type: application/json
     * - иначе (включая null) → Content-Type: application/xml
     */
    public static HttpHeaders build(String type) {
        HttpHeaders headers = new HttpHeaders();
        if (isJson(type)) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        } else {
            headers.setContentType(MediaType.APPLICATION_XML);
        }
        return headers;
    }

    /**
     * Расширенная версия: добавляем произвольные заголовки с фильтрацией hop-by-hop.
     * Если extra содержит Content-Type и allowOverrideContentType=true — переопределим.
     */
    public static HttpHeaders build(String type,
                                    Map<String, String> extra,
                                    boolean allowOverrideContentType) {
        HttpHeaders headers = build(type);
        if (extra == null || extra.isEmpty()) {
            return headers;
        }
        for (Map.Entry<String, String> e : extra.entrySet()) {
            String name = e.getKey();
            String value = e.getValue();
            if (name == null || name.trim().isEmpty() || value == null) {
                continue;
            }
            String lower = name.toLowerCase();
            if (HOP_BY_HOP.contains(lower)) {
                continue; // не отдаём hop-by-hop наружу
            }
            if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
                if (allowOverrideContentType) {
                    // стараемся распарсить как MediaType; если не получилось — ставим как строку
                    try {
                        headers.setContentType(MediaType.parseMediaType(value));
                    } catch (IllegalArgumentException ex) {
                        headers.set(HttpHeaders.CONTENT_TYPE, value);
                    }
                }
                // если нельзя переопределять — пропускаем Content-Type из extra
                continue;
            }
            headers.set(name, value);
        }
        return headers;
    }

    /** Утилита для добавления заголовков к уже готовым headers (например, после выбора ответа). */
    public static void merge(HttpHeaders target,
                             Map<String, String> extra,
                             boolean allowOverrideContentType) {
        if (target == null || extra == null || extra.isEmpty()) return;
        for (Map.Entry<String, String> e : extra.entrySet()) {
            String name = e.getKey();
            String value = e.getValue();
            if (name == null || name.trim().isEmpty() || value == null) continue;

            String lower = name.toLowerCase();
            if (HOP_BY_HOP.contains(lower)) continue;

            if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
                if (allowOverrideContentType) {
                    try {
                        target.setContentType(MediaType.parseMediaType(value));
                    } catch (IllegalArgumentException ex) {
                        target.set(HttpHeaders.CONTENT_TYPE, value);
                    }
                }
                continue;
            }
            target.set(name, value);
        }
    }

    private static boolean isJson(String type) {
        return type != null && "json".equalsIgnoreCase(type);
    }
}
