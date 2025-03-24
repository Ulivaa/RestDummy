package com.LT.restDummy.domain.response;

public class StubResponse {

    // Универсальный идентификатор: может быть вес (THRESHOLD), номер (PARAM_BASED), или null (DEFAULT)
    private final Integer key;

    // Содержимое ответа
    private final String content;

    // Тип логики: DEFAULT, THRESHOLD, PARAM_BASED
    private final ResponseType type;

    // Только для PARAM_BASED
    private final String paramName;
    private final String paramValue;

    // ----------- Конструкторы -----------

    // DEFAULT
    public StubResponse(String content) {
        this(null, content, ResponseType.DEFAULT, null, null);
    }

    // THRESHOLD
    public StubResponse(int weight, String content) {
        this(weight, content, ResponseType.THRESHOLD, null, null);
    }

    // PARAM_BASED
    public StubResponse(int responseNum, String content, String paramName, String paramValue) {
        this(responseNum, content, ResponseType.PARAM_BASED, paramName, paramValue);
    }

    // Общий приватный конструктор
    private StubResponse(Integer key, String content, ResponseType type, String paramName, String paramValue) {
        this.key = key;
        this.content = content;
        this.type = type;
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    // ----------- Геттеры -----------

    public Integer getKey() {
        return key;
    }

    public String getContent() {
        return content;
    }

    public ResponseType getType() {
        return type;
    }

    public String getParamName() {
        return paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    @Override
    public String toString() {
        return "StubResponse{" +
                "key=" + key +
                ", type=" + type +
                ", paramName='" + paramName + '\'' +
                ", paramValue='" + paramValue + '\'' +
                '}';
    }
}
