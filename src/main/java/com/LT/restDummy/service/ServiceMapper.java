package com.LT.restDummy.service;

import com.LT.restDummy.domain.dto.ServiceRequestDto;
import com.LT.restDummy.domain.model.StubService;

public final class ServiceMapper {

    private ServiceMapper() {
        // utility class
    }

    public static ServiceRequestDto serviceToDto(StubService service) {
        return new ServiceRequestDto(
                service.getName(),
                service.getDelayConfig().getDefaultDelay(),
                service.getDelayConfig().getCurrentDelay(),
                service.getDelayConfig().getTimeout(),
                service.isAvailable(),
                service.getSystemName()
        );
    }

    public static StubService dtoToService(ServiceRequestDto dto) {
        return new StubService(
                dto.getName(),
                dto.getDelay(),    // и как delay, и как defaultDelay
                dto.getTimeout(),
                dto.isAvailable(),
                dto.getSystemName()
        );
    }
}
