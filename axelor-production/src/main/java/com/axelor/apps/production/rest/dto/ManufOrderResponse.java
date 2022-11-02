package com.axelor.apps.production.rest.dto;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.tool.api.ResponseStructure;

public class ManufOrderResponse extends ResponseStructure {

  private final Long id;
  private final Integer status;

  public ManufOrderResponse(ManufOrder manufOrder) {
    super(manufOrder.getVersion());
    this.id = manufOrder.getId();
    this.status = manufOrder.getStatusSelect();
  }

  public Long getId() {
    return id;
  }

  public Integer getStatus() {
    return status;
  }
}
