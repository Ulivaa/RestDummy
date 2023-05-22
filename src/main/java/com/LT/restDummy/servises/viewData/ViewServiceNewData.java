package com.LT.restDummy.servises.viewData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Необходим для отображения через GUI
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewServiceNewData {
    private String serviceName;
    private String content;


}
