package com.LT.restDummy.service;

import com.LT.restDummy.domain.dto.ServiceRequestDto;
import com.LT.restDummy.domain.model.StubService;

public class ServiceMapper {
    ServiceValue serviceValue;

    public ServiceMapper(ServiceValue serviceValue) {
        this.serviceValue = serviceValue;
    }

    public static ServiceRequestDto serviceToDto(StubService service) {
        return new ServiceRequestDto(
                service.getName(),
                service.getDelayConfig().getDefaultDelay(),
                service.getDelayConfig().getCurrentDelay(), // delay = currentDelay
                service.getDelayConfig().getTimeout(),
                service.isAvailable(),
                service.getSystemName()
        );
    }

    public static StubService dtoToService(ServiceRequestDto dto) {
        return new StubService(
                dto.getName(),
                dto.getDelay(),    // используется и как delay, и как defaultDelay
                dto.getTimeout(),
                dto.isAvailable(),
                dto.getSystemName()
        );
    }
}
