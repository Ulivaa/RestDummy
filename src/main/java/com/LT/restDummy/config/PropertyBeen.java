package com.LT.restDummy.config;

import com.LT.restDummy.file.FileWork;
import com.LT.restDummy.servises.Service;
import com.LT.restDummy.servises.ServiceValue;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Класс создает бины для работы с сервисами, их задержками и доступностью.
 * Инициализирует сервисы из файлов, если они есть в папке
 */
@Configuration
@Slf4j
@PropertySource("file:properties")
@PropertySource("file:servicesParams.properties")
public class PropertyBeen {
    List<String> allFiles = FileWork.getListFilesForFolder(new File("services"));

    @SneakyThrows
    @Bean("Services")
    public ServiceValue getFileServices() {
        HashMap<String, Service> services = new HashMap<>();
        Properties properties = new Properties();
        properties.load(new FileInputStream("servicesParams.properties"));
        for (String fileName : allFiles) {
            BufferedReader reader = new BufferedReader(new FileReader("services/" + fileName));
            String fileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            String endpoint = properties.getProperty(fileName + ".endpoint");
            if (endpoint != null) {
                services.put(endpoint, FileWork.getService(fileName, fileContent));
            } else {
                services.put(fileName, FileWork.getService(fileName, fileContent));
            }
        }
        return ServiceValue.getInstance().initialize(services);
    }

    /**
     * проверяем нужно ли использовать дополнительные настройки для использования смешано http и https
     */
    @SneakyThrows
    @ConditionalOnProperty(value = "use.http.https", havingValue = "true")
    @Bean("ServletWebServerFactory")
    public ServletWebServerFactory servletContainer() {
        Properties properties = new Properties();
        properties.load(new FileInputStream("properties"));
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createStandardConnector(Integer.parseInt(properties.getProperty("server.http.port"))));
        return tomcat;
    }

    private Connector createStandardConnector(int httpPort) {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(httpPort);
        return connector;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}