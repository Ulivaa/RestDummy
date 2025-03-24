package com.LT.restDummy.service;

import com.LT.restDummy.domain.dto.ServiceRequestDto;
import com.LT.restDummy.domain.model.Service;

public class ServiceMapper {
    ServiceValue serviceValue;

    public ServiceMapper(ServiceValue serviceValue) {
        this.serviceValue = serviceValue;
    }

    public static ServiceRequestDto serviceToDto(Service service) {
        return new ServiceRequestDto(
                service.getName(),
                service.getDelayConfig().getDefaultDelay(),
                service.getDelayConfig().getCurrentDelay(), // delay = currentDelay
                service.getDelayConfig().getTimeout(),
                service.isAvailable(),
                service.getSystemName()
        );
    }

    public static Service dtoToService(ServiceRequestDto dto) {
        return new Service(
                dto.getName(),
                dto.getDelay(),    // используется и как delay, и как defaultDelay
                dto.getTimeout(),
                dto.isAvailable(),
                dto.getSystemName()
        );
    }
}
