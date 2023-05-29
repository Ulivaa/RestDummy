package com.LT.restDummy.Config;

//import com.LT.restDummy.delay.model.DelayValue;

import com.LT.restDummy.file.FileWork;
import com.LT.restDummy.servises.Service;
import com.LT.restDummy.servises.ServiceValue;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Класс создает бины для работы с сервисами, их задержками и доступностью.
Инициализирует сервисы из файлов, если они есть в папке
 */
@Configuration
@Slf4j
public class PropertyBeen {
    ArrayList<String> allFiles = FileWork.getListFilesForFolder(new File("services"));

    @SneakyThrows
    @Bean("Services")
    public ServiceValue getFileServices() {
        HashMap<String, Service> services = new HashMap<>();

        for (String fileName : allFiles) {
            BufferedReader reader = new BufferedReader(new FileReader("services/" + fileName));
            String response = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            String endpoint = FileWork.getContentEndPoint(response);
            if (endpoint != null) {
                services.put(endpoint, FileWork.getService(response, fileName));
            } else {
                services.put(fileName, FileWork.getService(response, fileName));
            }
        }
        return ServiceValue.getInstance().initialize(services);
    }
}