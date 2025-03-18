package com.LT.restDummy;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.FileInputStream;
import java.util.Properties;

@Slf4j
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class RestDummyApplication {

    @SneakyThrows
    public static void main(String[] args) {

        Properties properties = new Properties();
        properties.load(new FileInputStream("properties"));
        System.setProperty("server.port", properties.getProperty("server.port"));
        System.setProperty("logging.level.root", properties.getProperty("logging.level.root"));
//        VictoriaWriter.getInstance().initialize(properties);
/**
 * проверяем нужно ли использовать дополнительные настройки с https
 */
        if (properties.getProperty("use.https") != null && properties.getProperty("use.https").equals("true")
                || (properties.getProperty("use.http.https") != null && properties.getProperty("use.http.https").equals("true"))) {
            System.setProperty("server.ssl.enabled-protocols", "TLSv1.2");
            System.setProperty("server.ssl.key-store-type", "JKS");
            System.setProperty("server.ssl.key-store", "classpath:certificates/stub2.jks");
            System.setProperty("server.ssl.key-store-password", "STUB_SHARED_LT");
            System.setProperty("trust.store", "classpath:certificates/stub2.jks");
            System.setProperty("trust.store.password", "STUB_SHARED_LT");
        }
        SpringApplication.run(RestDummyApplication.class, args);
    }
}