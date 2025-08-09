package com.LT.restDummy.helper;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;

@UtilityClass
public class ResponseHeaderBuilder {

    public static HttpHeaders build(String type) {
        HttpHeaders headers = new HttpHeaders();
        if ("json".equalsIgnoreCase(type)) {
            headers.add("Content-Type", "application/json");
        } else {
            headers.add("Content-Type", "application/xml");
        }
        return headers;
    }
}
