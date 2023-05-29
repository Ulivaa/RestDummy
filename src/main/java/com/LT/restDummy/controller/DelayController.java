package com.LT.restDummy.controller;

import com.LT.restDummy.file.FileWork;
import com.LT.restDummy.servises.ServiceValue;
import com.LT.restDummy.servises.viewData.ViewServiceData;
import com.LT.restDummy.servises.viewData.ViewServiceDataDTO;
import com.LT.restDummy.servises.viewData.ViewServiceNewData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/*
Класс взаимодействует с фронтом(html) и управляет задержкой и доступностью сервисов
*/
@Slf4j
@Controller
public class DelayController {
    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final ServiceValue serviceValue;

    @Autowired
    public DelayController(ServiceValue serviceValue) {
        this.serviceValue = serviceValue;
    }

/**
      страница с информацией по доступности сервиса, задержке и таймаутам
*/
    @GetMapping("/delay")
    public String showNewDelayForm(Model model) {
        getForm(model);
        return "delay";
    }

/**
      страница редактирования доступности сервиса и задержки
*/
    @GetMapping(value = "/delay/edit")
    public String editDelayForm(Model model) {
        getForm(model);
        return "edit";
    }

/**
        сохранение информации после редактирования
*/
    @RequestMapping(value = "/delay/save")
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
      ставит отклик -10% от таймаута
*/
    @RequestMapping("/delay/calculate")
    public String calculateDelaySet() {
        serviceValue.setMinus10PercentDelay();
        return "redirect:/delay";
    }

/**
        вернуть дефолтные задержки
*/
    @RequestMapping("/delay/default")
    public String defaultDelaySet() {
        serviceValue.setDefaultDelayForAllService();
        return "redirect:/delay";
    }

/**
        включить все сервисы
*/
    @RequestMapping("/delay/enableServices")
    public String enableServices() {
        serviceValue.setAvailabilityToAllService(true);
        return "redirect:/delay";
    }

/**
        выключить все сервисы
*/
    @RequestMapping("/delay/disableServices")
    public String disableServices() {
        serviceValue.setAvailabilityToAllService(false);
        return "redirect:/delay";
    }

    @RequestMapping("/services/add")
    public String addService(Model model) {
        ViewServiceNewData viewServiceNewData = new ViewServiceNewData("", "");
        model.addAttribute("viewServiceNewData", viewServiceNewData);
        return "newServices";
    }

/**
        Сохранение сервиса. Если есть параметр эндпоинта, то он становится именем сервиса(но не файла)
*/
    @RequestMapping(value = "/services/save")
    public String saveAddForm(@ModelAttribute(name = "viewServiceData") ViewServiceNewData viewData, Model model) {
        String ServiceNameOrEndpoint = FileWork.getContentEndPoint(viewData.getContent());
        FileWork.fullFile(viewData.getServiceName(), viewData.getContent());
        if (ServiceNameOrEndpoint == null) {
            ServiceNameOrEndpoint = viewData.getServiceName();
        }
        serviceValue.setService(ServiceNameOrEndpoint,
                FileWork.getService(viewData.getContent(), viewData.getServiceName()));
        return "redirect:/delay";
    }

    @RequestMapping(value = "/services/update")
    public String updateForm(@ModelAttribute(name = "viewServiceNewData") ViewServiceNewData viewData, Model model) {
        String content = serviceValue.getFullFileByService(viewData.getServiceName());
        if (content == null || content.isEmpty()) {
            viewData.setContent("Файла не существует или он пуст.");
        } else {
            viewData.setContent(content);
        }
        model.addAttribute("viewServiceNewData", viewData);
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
                    serviceValue.getSchedulerToAvailabilityByService(item).format(DATE_TIME_FORMATTER)));
        }
        for (ViewServiceData item : dataList) {
            form.addViewServiceData(item);
        }
        model.addAttribute("form", form);
        return model;
    }
}