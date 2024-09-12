package com.axelor.apps.sale.rest.dto;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class SaleOrderPutRequest extends RequestPostStructure {
  public static final String SALE_ORDER_UPDATE_CONFIRM = "confirm";
  public static final String SALE_ORDER_UPDATE_FINALIZE = "finalize";
  @NotNull private Long saleOrderId;

  @Pattern(
      regexp = SALE_ORDER_UPDATE_FINALIZE + "|" + SALE_ORDER_UPDATE_CONFIRM,
      flags = Pattern.Flag.CASE_INSENSITIVE)
  @NotNull
  private String toStatus;

  public Long getSaleOrderId() {
    return saleOrderId;
  }

  public void setSaleOrderId(Long saleOrderId) {
    this.saleOrderId = saleOrderId;
  }

  public String getToStatus() {
    return toStatus;
  }

  public void setToStatus(String toStatus) {
    this.toStatus = toStatus;
  }

  public SaleOrder fetchSaleOrder() {
    if (saleOrderId == null || saleOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(SaleOrder.class, saleOrderId, ObjectFinder.NO_VERSION);
  }
}
