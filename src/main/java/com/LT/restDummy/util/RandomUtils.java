package com.LT.restDummy.util;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;

@UtilityClass
public class RandomUtils {

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String CHAR_LOWER_RQUID = "abcdef";
    private static final String CHAR_UPPER_RQUID = CHAR_LOWER_RQUID.toUpperCase();
    private static final String NUMBER = "0123456789";

    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER;
    private static final String DATA_FOR_RANDOM_STRING_NUMBER = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static final String DATA_FOR_RQUID = CHAR_LOWER_RQUID + CHAR_UPPER_RQUID + NUMBER;

    private static final SecureRandom random = new SecureRandom();

    public static String randomChar(int length) {
        return generate(length, DATA_FOR_RANDOM_STRING);
    }

    public static String randomNumber(int length) {
        return generate(length, NUMBER);
    }

    public static String randomNumberAndChar(int length) {
        return generate(length, DATA_FOR_RANDOM_STRING_NUMBER);
    }

    public static String randomRqUID(int length) {
        return generate(length, DATA_FOR_RQUID);
    }

    private static String generate(int length, String dataSource) {
        if (length < 1) throw new IllegalArgumentException("length must be >= 1");
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndIndex = random.nextInt(dataSource.length());
            sb.append(dataSource.charAt(rndIndex));
        }
        return sb.toString();
    }
}
