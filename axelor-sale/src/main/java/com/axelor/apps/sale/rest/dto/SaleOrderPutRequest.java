package com.axelor.apps.sale.rest.dto;

import com.axelor.utils.api.RequestStructure;

public class SaleOrderPutRequest extends RequestStructure {

  SaleOrderLinePostRequest saleOrderLine;

  public SaleOrderLinePostRequest getSaleOrderLine() {
    return saleOrderLine;
  }

  public void setSaleOrderLine(SaleOrderLinePostRequest saleOrderLine) {
    this.saleOrderLine = saleOrderLine;
  }
}
