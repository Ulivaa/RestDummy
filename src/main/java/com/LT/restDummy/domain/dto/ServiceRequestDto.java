package com.LT.restDummy.domain.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequestDto {

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("defaultDelay")
    @Expose
    private Long defaultDelay;

    @SerializedName("delay")
    @Expose
    private Long delay; // используется как текущее значение (currentDelay)

    @SerializedName("timeout")
    @Expose
    private Long timeout;

    @SerializedName("available")
    @Expose
    private boolean available;

    @SerializedName("systemName")
    @Expose
    private String systemName;
}
