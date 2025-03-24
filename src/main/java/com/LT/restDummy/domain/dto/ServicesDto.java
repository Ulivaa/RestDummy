package com.LT.restDummy.domain.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServicesDto {
    @SerializedName("services")
    @Expose
    List<ServiceRequestDto> services;

    public ServicesDto() {
    }
}