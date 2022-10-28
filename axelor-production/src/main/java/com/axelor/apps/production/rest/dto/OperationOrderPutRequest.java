package com.axelor.apps.production.rest.dto;

import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.tool.api.RequestStructure;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class OperationOrderPutRequest extends RequestStructure {

  @NotNull
  @Min(OperationOrderRepository.STATUS_DRAFT)
  @Max(OperationOrderRepository.STATUS_FINISHED)
  private Integer status;

  public OperationOrderPutRequest() {}

  public int getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }
}
