package com.axelor.apps.sale.rest.dto;

import com.axelor.utils.api.RequestStructure;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class SaleOrderPutRequest extends RequestStructure {
  public static final String SALE_ORDER_UPDATE_CONFIRM = "confirm";
  public static final String SALE_ORDER_UPDATE_FINALIZE = "finalize";

  @Pattern(
      regexp = SALE_ORDER_UPDATE_FINALIZE + "|" + SALE_ORDER_UPDATE_CONFIRM,
      flags = Pattern.Flag.CASE_INSENSITIVE)
  @NotNull
  private String toStatus;

  public String getToStatus() {
    return toStatus;
  }

  public void setToStatus(String toStatus) {
    this.toStatus = toStatus;
  }
}
