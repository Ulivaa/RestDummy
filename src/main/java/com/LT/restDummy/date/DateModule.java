package com.LT.restDummy.date;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateModule {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.'123+03:00'");

    public static String get_date_now() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }
}