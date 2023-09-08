package com.axelor.apps.hr.rest.dto;

import com.axelor.utils.api.RequestStructure;
import javax.validation.constraints.NotNull;

public class ExpenseRefusalPutRequest extends RequestStructure {

  @NotNull private String groundForRefusal;

  public String getGroundForRefusal() {
    return groundForRefusal;
  }

  public void setGroundForRefusal(String groundForRefusal) {
    this.groundForRefusal = groundForRefusal;
  }
}
