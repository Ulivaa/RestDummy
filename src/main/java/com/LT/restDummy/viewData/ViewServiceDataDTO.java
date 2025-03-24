package com.LT.restDummy.viewData;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Необходим для отображения через GUI
 */
@Getter
@Setter
public class ViewServiceDataDTO {
    private List<ViewServiceData> viewData;

    public ViewServiceDataDTO() {
        this.viewData = new ArrayList<>();
    }

    //    Не удалять. Без него ломается HTML
    public ViewServiceDataDTO(List<ViewServiceData> viewData) {
        this.viewData = viewData;
    }

    public void addViewServiceData(ViewServiceData data) {
        this.viewData.add(data);
    }

    public List<ViewServiceData> getViewService() {
        return viewData;
    }

    //    Не удалять. Без него ломается HTML
    public void setViewData(List<ViewServiceData> viewData) {
        this.viewData = viewData;
    }
}