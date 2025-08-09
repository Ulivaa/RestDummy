package com.LT.restDummy.date;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static LocalDateTime nowTruncatedToMinutes() {
        return LocalDateTime.parse(LocalDateTime.now().format(FORMATTER), FORMATTER);
    }
}
