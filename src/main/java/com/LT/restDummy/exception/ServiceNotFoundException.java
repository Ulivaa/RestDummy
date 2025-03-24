package com.LT.restDummy.exception;

/**
 * Исключение, выбрасываемое, если сервис не найден в реестре.
 */
public class ServiceNotFoundException extends RuntimeException {

    public ServiceNotFoundException(String serviceName) {
        super("Сервис с именем '" + serviceName + "' не найден.");
    }
}
