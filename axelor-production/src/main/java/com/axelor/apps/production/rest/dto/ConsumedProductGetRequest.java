package com.axelor.apps.production.rest.dto;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestPostStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ConsumedProductGetRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long manufOrderId;

  @NotNull
  @Min(0)
  private Integer manufOrderVersion;

  public ConsumedProductGetRequest() {}

  public Long getManufOrderId() {
    return manufOrderId;
  }

  public void setManufOrderId(Long manufOrderId) {
    this.manufOrderId = manufOrderId;
  }

  public Integer getManufOrderVersion() {
    return manufOrderVersion;
  }

  public void setManufOrderVersion(Integer manufOrderVersion) {
    this.manufOrderVersion = manufOrderVersion;
  }

  // Transform id to object
  public ManufOrder fetchManufOrder() {
    return ObjectFinder.find(ManufOrder.class, manufOrderId, manufOrderVersion);
  }
}
