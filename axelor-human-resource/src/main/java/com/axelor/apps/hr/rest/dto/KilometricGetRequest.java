package com.axelor.apps.hr.rest.dto;

import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.NotNull;

public class KilometricGetRequest extends RequestPostStructure {
  @NotNull private String fromCity;
  @NotNull private String toCity;

  public String getFromCity() {
    return fromCity;
  }

  public void setFromCity(String fromCity) {
    this.fromCity = fromCity;
  }

  public String getToCity() {
    return toCity;
  }

  public void setToCity(String toCity) {
    this.toCity = toCity;
  }
}
