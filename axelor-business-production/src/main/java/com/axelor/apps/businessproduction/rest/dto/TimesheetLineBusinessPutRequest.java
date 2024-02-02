package com.axelor.apps.businessproduction.rest.dto;

import com.axelor.apps.hr.rest.dto.TimesheetLinePutRequest;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.utils.api.ObjectFinder;
import javax.validation.constraints.Min;

public class TimesheetLineBusinessPutRequest extends TimesheetLinePutRequest {

  @Min(0)
  private Long manufOrderId;

  @Min(0)
  private Long operationOrderId;

  public Long getManufOrderId() {
    return manufOrderId;
  }

  public void setManufOrderId(Long manufOrderId) {
    this.manufOrderId = manufOrderId;
  }

  public Long getOperationOrderId() {
    return operationOrderId;
  }

  public void setOperationOrderId(Long operationOrderId) {
    this.operationOrderId = operationOrderId;
  }

  public ManufOrder fetchManufOrder() {
    if (manufOrderId == null || manufOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(ManufOrder.class, manufOrderId, ObjectFinder.NO_VERSION);
  }

  public OperationOrder fetchOperationOrder() {
    if (operationOrderId == null || operationOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(OperationOrder.class, operationOrderId, ObjectFinder.NO_VERSION);
  }
}
