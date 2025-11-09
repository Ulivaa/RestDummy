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

            // Собираем базовые имена сервисов (без суффиксов -1, -2 и т.д.)
            HashMap<String, String> serviceBaseNames = new HashMap<>();
            for (String fileName : allFiles) {
                // Если файл имеет формат name-N, извлекаем базовое имя
                if (fileName.matches(".+-\\d+$")) {
                    String baseName = fileName.substring(0, fileName.lastIndexOf('-'));
                    serviceBaseNames.put(fileName, baseName);
                } else {
                    // Обычный файл - базовое имя = имя файла
                    serviceBaseNames.put(fileName, fileName);
                }
            }

            // Группируем файлы по базовым именам
            HashMap<String, List<String>> groupedFiles = new HashMap<>();
            for (String fileName : allFiles) {
                String baseName = serviceBaseNames.get(fileName);
                groupedFiles.computeIfAbsent(baseName, k -> new java.util.ArrayList<>()).add(fileName);
            }

            // Загружаем сервисы
            for (String baseName : groupedFiles.keySet()) {
                List<String> files = groupedFiles.get(baseName);
                
                // Проверяем, есть ли файл без суффикса
                String mainFile = files.stream()
                        .filter(f -> !f.matches(".+-\\d+$"))
                        .findFirst()
                        .orElse(null);
                
                String fileContent = "";
                if (mainFile != null) {
                    // Есть основной файл - читаем его
                    try (BufferedReader reader = new BufferedReader(new FileReader("services/" + mainFile))) {
                        fileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                    }
                } else {
                    // Нет основного файла, только файлы с суффиксами (например, example-1, example-2)
                    // Передаем пустое содержимое - ResponseBuilder сам загрузит множественные файлы
                    fileContent = "";
                }
                
                String endpoint = properties.getProperty(baseName + ".endpoint");
                String serviceKey = endpoint != null ? endpoint : baseName;
                services.put(serviceKey, ServiceFileHandler.getService(baseName, fileContent));
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
