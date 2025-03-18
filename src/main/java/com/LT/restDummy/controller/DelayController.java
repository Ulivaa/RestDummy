package com.LT.restDummy.controller;

import com.LT.restDummy.file.FileWork;
import com.LT.restDummy.servises.Service;
import com.LT.restDummy.servises.ServiceValue;
import com.LT.restDummy.servises.viewData.ViewServiceData;
import com.LT.restDummy.servises.viewData.ViewServiceDataDTO;
import com.LT.restDummy.servises.viewData.ViewServiceNewData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс взаимодействует с фронтом(html) и управляет задержкой и доступностью сервисов
 */
@Slf4j
@Controller
@ConfigurationProperties
public class DelayController {
    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final ServiceValue serviceValue;
    private final Pattern pattern = Pattern.compile("\\.(.+)=(.+)");
    private String subsystem;

    @Autowired
    public DelayController(ServiceValue serviceValue) throws IOException {
        this.serviceValue = serviceValue;
        Properties properties = new Properties();
        properties.load(new FileInputStream("properties"));
        this.subsystem = properties.getProperty("subsystem");
    }

    /**
     * страница с информацией по доступности сервиса, задержке и таймаутам
     */
    @GetMapping("/delay")
    public String showNewDelayForm(Model model) {
        getForm(model);
        return "delay";
    }

    /**
     * страница редактирования доступности сервиса и задержки
     */
    @GetMapping("/delay/edit")
    public String editDelayForm(Model model) {
        model.addAttribute("subsystem", subsystem);
        getForm(model);
        return "edit";
    }

    /**
     * сохранение информации после редактирования
     */
    @RequestMapping("/delay/save")
    public String saveEditForm(@ModelAttribute(name = "form") ViewServiceDataDTO viewData, Model model) {
        for (ViewServiceData service : viewData.getViewData()) {
            serviceValue.setNewDelayToService(service.getName(), service.getCurrentDelay());
            serviceValue.setNewDelayToScheduler(service.getName(), service.getDelayForScheduler());
            serviceValue.setSchedulerToDelay(service.getName(), LocalDateTime.parse(service.getSchedulerDelay(), DATE_TIME_FORMATTER));
            serviceValue.setAvailabilityToService(service.getName(), service.getIsAvailable());
            serviceValue.setSchedulerForAvailabilityToService(service.getName(), LocalDateTime.parse(service.getSchedulerAvailability(), DATE_TIME_FORMATTER));
        }
        return "redirect:/delay";
    }

    /**
     * ставит отклик -10% от таймаута
     */
    @RequestMapping("/delay/calculate")
    public String calculateDelaySet() {
        serviceValue.setMinus10PercentDelay();
        return "redirect:/delay";
    }

    /**
     * вернуть дефолтные задержки
     */
    @RequestMapping("/delay/default")
    public String defaultDelaySet() {
        serviceValue.setDefaultDelayForAllService();
        return "redirect:/delay";
    }

    /**
     * включить все сервисы
     */
    @RequestMapping("/delay/enableServices")
    public String enableServices() {
        serviceValue.setAvailabilityToAllService(true);
        return "redirect:/delay";
    }

    /**
     * выключить все сервисы
     */
    @RequestMapping("/delay/disableServices")
    public String disableServices() {
        serviceValue.setAvailabilityToAllService(false);
        return "redirect:/delay";
    }

    @RequestMapping("/services/add")
    public String addService(Model model) {
        ViewServiceNewData viewServiceNewData = new ViewServiceNewData("", "", "", "");
        model.addAttribute("viewServiceNewData", viewServiceNewData);
        return "newServices";
    }

    /**
     * Сохранение сервиса. Если есть параметр эндпоинта, то он становится именем сервиса(но не файла)
     */
    @RequestMapping("/services/save")
    public String saveAddForm(@ModelAttribute(name = "viewServiceData") ViewServiceNewData viewData, Model model) {
        HashMap<String, String> params = new HashMap<>();
        Matcher matcher = pattern.matcher(viewData.getParams());
        while (matcher.find()) {
            params.put(matcher.group(1), matcher.group(2));
        }
        String ServiceNameOrEndpoint = params.getOrDefault("endpoint", null);
        if (!params.containsKey("endpoint")) {
            ServiceNameOrEndpoint = viewData.getServiceName();
        }
        serviceValue.setService(ServiceNameOrEndpoint,
                FileWork.getService(viewData.getServiceName(), viewData.getContent(), params));
        FileWork.updateFilesServices(viewData.getServiceName(), viewData.getContent(), viewData.getParams());
        return "redirect:/delay";
    }

    @RequestMapping("/services/update")
    public String updateForm(@ModelAttribute(name = "viewServiceNewData") ViewServiceNewData viewData, Model model) {
        Service service = serviceValue.getServiceByName(viewData.getServiceName());
        String content = service.getFullServiceFile();
        String params = service.getName() + ".type=" + service.getType() + "\n" +
                service.getName() + ".timeout=" + service.getTimeout() + "\n" +
                service.getName() + ".delay=" + service.getDefaultDelay() + "\n";
        if (service.getEndpoint() != null) {
            params = params + service.getName() + ".endpoint=" + service.getEndpoint() + "\n";
        }
        if (service.getThresholds() != null) {
            params = params + service.getName() + ".threshold=" + service.getThresholds().toString().replace(" ", "") + "\n";
        }
        if (service.getSystemName() != null) {
            params = params + service.getName() + ".systemName=" + service.getSystemName() + "\n";
        }
        if (service.isChangeableParam()) {
            if (service.getChangeableParamValue() != null) {
                params = params + service.getName() + ".param.value=" + service.getChangeableParamValue() + "\n";
            }
            if (service.getChangeableParamName() != null) {
                params = params + service.getName() + ".param.name=" + service.getChangeableParamName() + "\n";
            }
            if (service.getChangeableParamResponse() != null) {
                params = params + service.getName() + ".param.responseNum=" + service.getChangeableParamResponse() + "\n";
            }
        }
        if (content == null || content.isEmpty()) {
            viewData.setContent("Файла не существует или он пуст.");
        } else {
            viewData.setContent(content);
        }
        viewData.setParams(params);
        model.addAttribute("viewServiceNewData", viewData);
        model.addAttribute("subsystem", subsystem);
        return "newServices";
    }

    private Model getForm(Model model) {
        List<String> services = serviceValue.getServicesName();
        List<ViewServiceData> dataList = new ArrayList<>();
        ViewServiceDataDTO form = new ViewServiceDataDTO();
        for (String item : services) {
            dataList.add(new ViewServiceData(item,
                    serviceValue.getDelayByService(item),
                    serviceValue.getTimeoutByService(item),
                    serviceValue.getDelayForSchedulerByService(item),
                    serviceValue.getSchedulerToDelayByService(item).format(DATE_TIME_FORMATTER),
                    serviceValue.getAvailabilityByService(item),
                    serviceValue.getSchedulerToAvailabilityByService(item).format(DATE_TIME_FORMATTER),
                    serviceValue.getSystemNameByService(item)));
        }
        for (ViewServiceData item : dataList) {
            form.addViewServiceData(item);
        }
        model.addAttribute("form", form);
        model.addAttribute("subsystem", subsystem);
        return model;
    }
}