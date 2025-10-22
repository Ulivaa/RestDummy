package com.LT.restDummy.config;

import com.LT.restDummy.domain.model.StubService;
import com.LT.restDummy.file.ServiceFileHandler;
import com.LT.restDummy.service.ServiceValue;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Класс создаёт бины для работы с сервисами, их задержками и доступностью.
 * Инициализирует сервисы из файлов, если они есть в папке.
 */
@Configuration
@Slf4j
@PropertySource("file:properties")
@PropertySource("file:servicesParams.properties")
public class PropertyBeen {

    private final List<String> allFiles = ServiceFileHandler.getListFilesForFolder(new File("services"));

    /**
     * Инициализация реестра сервисов из файлов.
     * ВАЖНО: Не создаёт новый ServiceValue, а использует внедрённый бин.
     */
    @Bean
    @SneakyThrows
    public org.springframework.beans.factory.InitializingBean loadFileServices(ServiceValue serviceValue) {
        return () -> {
            HashMap<String, StubService> services = new HashMap<>();
            Properties properties = new Properties();

            try (FileInputStream fis = new FileInputStream("servicesParams.properties")) {
                properties.load(fis);
            }

            for (String fileName : allFiles) {
                try (BufferedReader reader = new BufferedReader(new FileReader("services/" + fileName))) {
                    String fileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                    String endpoint = properties.getProperty(fileName + ".endpoint");
                    if (endpoint != null) {
                        services.put(endpoint, ServiceFileHandler.getService(fileName, fileContent));
                    } else {
                        services.put(fileName, ServiceFileHandler.getService(fileName, fileContent));
                    }
                }
            }

            serviceValue.initialize(services);
            log.info("Loaded {} stub service(s) from filesystem.", services.size());
        };
    }

    /**
     * Пул потоков для отложенных ответов (ResponseDelay).
     * daemon=true, читаемые имена, removeOnCancelPolicy=true, корректное завершение при остановке.
     */
    @Bean(name = "responseDelayExecutor", destroyMethod = "shutdown")
    public ScheduledExecutorService responseDelayExecutor() {
        ThreadFactory tf = new ThreadFactory() {
            private final AtomicInteger seq = new AtomicInteger(1);
            @Override public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "response-delay-" + seq.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        };

        ScheduledThreadPoolExecutor exec =
                new ScheduledThreadPoolExecutor(2, tf, new ThreadPoolExecutor.AbortPolicy());
        exec.setRemoveOnCancelPolicy(true);
        exec.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        return exec;
    }

    /**
     * Проверяем, нужно ли использовать дополнительные настройки для смешанного HTTP/HTTPS.
     */
    @SneakyThrows
    @ConditionalOnProperty(value = "use.http.https", havingValue = "true")
    @Bean("ServletWebServerFactory")
    public ServletWebServerFactory servletContainer() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("properties")) {
            properties.load(fis);
        }
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(
                createStandardConnector(Integer.parseInt(properties.getProperty("server.http.port")))
        );
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
