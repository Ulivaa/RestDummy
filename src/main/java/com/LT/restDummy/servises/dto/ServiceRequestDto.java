package com.LT.restDummy.servises.dto;

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

    private Long defaultDelay;
    @SerializedName("delay")
    @Expose
    private Long delay;
    private Long timeout;
    @SerializedName("available")
    @Expose
    private boolean available;
    private String systemName;
}