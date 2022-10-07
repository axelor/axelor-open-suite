package com.axelor.apps.production.rest.dto;

import com.axelor.apps.tool.api.RequestStructure;

public class ManufOrderPutRequest extends RequestStructure {

  private int status;

  public ManufOrderPutRequest() {}

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }
}
