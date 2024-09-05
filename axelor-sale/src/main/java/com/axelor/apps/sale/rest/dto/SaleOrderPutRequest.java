package com.axelor.apps.sale.rest.dto;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.NotNull;

public class SaleOrderPutRequest extends RequestPostStructure {
  @NotNull private Long saleOrderId;
  private Long statusId;

  public Long getSaleOrderId() {
    return saleOrderId;
  }

  public void setSaleOrderId(Long saleOrderId) {
    this.saleOrderId = saleOrderId;
  }

  public Long getStatusId() {
    return statusId;
  }

  public void setStatusId(Long statusId) {
    this.statusId = statusId;
  }

  public SaleOrder fetchSaleOrder() {
    if (saleOrderId == null || saleOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(SaleOrder.class, saleOrderId, ObjectFinder.NO_VERSION);
  }
}
