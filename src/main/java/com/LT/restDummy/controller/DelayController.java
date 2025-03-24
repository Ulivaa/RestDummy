package com.LT.restDummy.controller;

import com.LT.restDummy.file.ServiceFileHandler;
import com.LT.restDummy.domain.model.Service;
import com.LT.restDummy.service.ServiceValue;
import com.LT.restDummy.viewData.ViewServiceData;
import com.LT.restDummy.viewData.ViewServiceDataDTO;
import com.LT.restDummy.viewData.ViewServiceNewData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @GetMapping("/delay")
    public String showNewDelayForm(Model model) {
        getForm(model);
        return "delay";
    }

    @GetMapping("/delay/edit")
    public String editDelayForm(Model model) {
        model.addAttribute("subsystem", subsystem);
        getForm(model);
        return "edit";
    }

    @RequestMapping("/delay/save")
    public String saveEditForm(@ModelAttribute("form") ViewServiceDataDTO viewData, Model model) {
        for (ViewServiceData service : viewData.getViewData()) {
            serviceValue.delay().setDelay(service.getName(), service.getCurrentDelay());
            serviceValue.delay().setDelayForScheduler(service.getName(), service.getDelayForScheduler());
            serviceValue.delay().setSchedulerToDelay(service.getName(),
                    LocalDateTime.parse(service.getSchedulerDelay(), DATE_TIME_FORMATTER));
            serviceValue.availability().setAvailable(service.getName(), service.getIsAvailable());
            serviceValue.availability().scheduleAvailability(service.getName(),
                    LocalDateTime.parse(service.getSchedulerAvailability(), DATE_TIME_FORMATTER));
        }
        return "redirect:/delay";
    }

    @RequestMapping("/delay/calculate")
    public String calculateDelaySet() {
        serviceValue.delay().applyMinus10PercentToAll();
        return "redirect:/delay";
    }

    @RequestMapping("/delay/default")
    public String defaultDelaySet() {
        serviceValue.delay().setDefaultDelays();
        return "redirect:/delay";
    }

    @RequestMapping("/delay/enableServices")
    public String enableServices() {
        serviceValue.availability().setAvailableToAll(true);
        return "redirect:/delay";
    }

    @RequestMapping("/delay/disableServices")
    public String disableServices() {
        serviceValue.availability().setAvailableToAll(false);
        return "redirect:/delay";
    }

    @RequestMapping("/services/add")
    public String addService(Model model) {
        model.addAttribute("viewServiceNewData", new ViewServiceNewData("", "", "", ""));
        return "newServices";
    }

    @RequestMapping("/services/save")
    public String saveAddForm(@ModelAttribute("viewServiceData") ViewServiceNewData viewData, Model model) {
        Map<String, String> params = new HashMap<>();
        Matcher matcher = pattern.matcher(viewData.getParams());
        while (matcher.find()) {
            params.put(matcher.group(1), matcher.group(2));
        }

        String serviceName = params.getOrDefault("endpoint", viewData.getServiceName());

        Service service = ServiceFileHandler.getService(viewData.getServiceName(), viewData.getContent(), new HashMap<>(params));
        serviceValue.registry().register(serviceName, service);
        ServiceFileHandler.updateFilesServices(viewData.getServiceName(), viewData.getContent(), viewData.getParams());

        return "redirect:/delay";
    }

    @RequestMapping("/services/update")
    public String updateForm(@ModelAttribute("viewServiceNewData") ViewServiceNewData viewData, Model model) {
        Service service = serviceValue.registry().get(viewData.getServiceName());
        StringBuilder params = new StringBuilder();

        params.append(service.getName()).append(".type=").append(service.getType()).append("\n");
        params.append(service.getName()).append(".timeout=").append(service.getDelayConfig().getTimeout()).append("\n");
        params.append(service.getName()).append(".delay=").append(service.getDelayConfig().getDefaultDelay()).append("\n");

        if (service.getEndpoint() != null) {
            params.append(service.getName()).append(".endpoint=").append(service.getEndpoint()).append("\n");
        }
        if (service.getSystemName() != null) {
            params.append(service.getName()).append(".systemName=").append(service.getSystemName()).append("\n");
        }

        if (service.getFullServiceFile() == null || service.getFullServiceFile().isEmpty()) {
            viewData.setContent("Файла не существует или он пуст.");
        } else {
            viewData.setContent(service.getFullServiceFile());
        }

        viewData.setParams(params.toString());
        model.addAttribute("viewServiceNewData", viewData);
        model.addAttribute("subsystem", subsystem);
        return "newServices";
    }

    private Model getForm(Model model) {
        List<ViewServiceData> dataList = new ArrayList<>();
        for (String name : serviceValue.getServicesName()) {
            dataList.add(new ViewServiceData(
                    name,
                    serviceValue.delay().getDelay(name),
                    serviceValue.delay().getTimeout(name),
                    serviceValue.delay().getDelayForScheduler(name),
                    serviceValue.delay().getSchedulerToDelay(name).format(DATE_TIME_FORMATTER),
                    serviceValue.availability().isAvailable(name),
                    serviceValue.availability().getAvailabilityScheduler(name).format(DATE_TIME_FORMATTER),
                    serviceValue.getSystemNameByService(name)
            ));
        }
        ViewServiceDataDTO form = new ViewServiceDataDTO();
        form.setViewData(dataList);
        model.addAttribute("form", form);
        model.addAttribute("subsystem", subsystem);
        return model;
    }
}
