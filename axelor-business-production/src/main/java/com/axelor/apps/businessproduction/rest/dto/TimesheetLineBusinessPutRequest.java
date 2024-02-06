package com.axelor.apps.businessproduction.rest.dto;

import com.axelor.apps.hr.rest.dto.TimesheetLinePutRequest;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.utils.api.ObjectFinder;
import javax.validation.constraints.Min;

public class TimesheetLineBusinessPutRequest extends TimesheetLinePutRequest {

  @Min(0)
  private Long manufOrderId;

  @Min(0)
  private Long manufacturingOperationId;

  public Long getManufOrderId() {
    return manufOrderId;
  }

  public void setManufOrderId(Long manufOrderId) {
    this.manufOrderId = manufOrderId;
  }

  public Long getManufacturingOperationId() {
    return manufacturingOperationId;
  }

  public void setManufacturingOperationId(Long manufacturingOperationId) {
    this.manufacturingOperationId = manufacturingOperationId;
  }

  public ManufOrder fetchManufOrder() {
    if (manufOrderId == null || manufOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(ManufOrder.class, manufOrderId, ObjectFinder.NO_VERSION);
  }

  public ManufacturingOperation fetchManufacturingOperation() {
    if (manufacturingOperationId == null || manufacturingOperationId == 0L) {
      return null;
    }
    return ObjectFinder.find(
        ManufacturingOperation.class, manufacturingOperationId, ObjectFinder.NO_VERSION);
  }
}
