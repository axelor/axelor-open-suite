package com.axelor.apps.sale.rest.dto;

import com.axelor.utils.api.RequestStructure;
import javax.validation.constraints.NotNull;

public class SaleOrderAddLinePutRequest extends RequestStructure {
  @NotNull SaleOrderLinePostRequest saleOrderLine;

  public SaleOrderLinePostRequest getSaleOrderLine() {
    return saleOrderLine;
  }

  public void setSaleOrderLine(SaleOrderLinePostRequest saleOrderLine) {
    this.saleOrderLine = saleOrderLine;
  }
}
