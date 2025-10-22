package com.LT.restDummy.service;

import com.LT.restDummy.domain.dto.ServiceRequestDto;
import com.LT.restDummy.domain.manager.ServiceRegistry;
import com.LT.restDummy.domain.model.StubService;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


@Service
public class ServiceManagementService {

    private final ServiceValue serviceValue;

    public ServiceManagementService(ServiceValue serviceValue) {
        this.serviceValue = serviceValue;
    }

    public JSONObject getServices(String version) {
        JSONObject result = new JSONObject();

        ServiceRegistry registry = (serviceValue != null) ? serviceValue.registry() : null;
        Collection<StubService> raw = (registry != null) ? registry.getAll() : null;
        Collection<StubService> safeRaw = (raw != null) ? raw : Collections.<StubService>emptyList();

        List<ServiceRequestDto> dtoList = new ArrayList<>(safeRaw.size());
        for (StubService s : safeRaw) {
            if (s != null) dtoList.add(ServiceMapper.serviceToDto(s));
        }

        result.put("success", Boolean.TRUE);
        result.put("version", version);
        result.put("services", dtoList);
        return result;
    }


    public JSONObject editServices(List<ServiceRequestDto> services) {
        if (services != null && !services.isEmpty()) {
            for (ServiceRequestDto dtoService : services) {
                if (dtoService == null) continue;
                serviceValue.updateService(ServiceMapper.dtoToService(dtoService));
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", Boolean.TRUE);
        return jsonObject;
    }
}
