package com.axelor.apps.tool.api;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class RequestStructure {

  @NotNull
  @Min(0)
  private Integer version;

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }
}
