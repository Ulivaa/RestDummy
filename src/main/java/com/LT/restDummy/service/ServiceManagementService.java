package com.LT.restDummy.service;

import com.LT.restDummy.domain.dto.ServiceRequestDto;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@RequiredArgsConstructor
@Service
public class ServiceManagementService {

    private final ServiceValue serviceValue;

    public JSONObject getServices(String version) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", true);
        jsonObject.put("services", serviceValue.registry().getAll()
                .stream().map(ServiceMapper::serviceToDto)
                .collect(Collectors.toList()));
        jsonObject.put("version", version);
        return jsonObject;
    }

    public JSONObject editServices(List<ServiceRequestDto> services) {
        for (ServiceRequestDto dtoService : services) {
            serviceValue.updateService(ServiceMapper.dtoToService(dtoService));
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", true);
        return jsonObject;
    }
}
