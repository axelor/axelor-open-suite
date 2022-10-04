package com.axelor.apps.production.rest.dto;

import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.tool.api.ResponseStructure;

public class OperationOrderResponse extends ResponseStructure {

  protected final Long id;
  protected final Integer status;

  public OperationOrderResponse(OperationOrder operationOrder) {
    super(operationOrder.getVersion());
    this.id = operationOrder.getId();
    this.status = operationOrder.getStatusSelect();
  }

  public Long getId() {
    return id;
  }

  public Integer getStatus() {
    return status;
  }
}
