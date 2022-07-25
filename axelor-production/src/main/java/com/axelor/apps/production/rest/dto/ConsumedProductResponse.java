package com.axelor.apps.production.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.tool.api.ResponseStructure;
import java.math.BigDecimal;

public class ConsumedProductResponse extends ResponseStructure {

  private final Long productId;
  private final String productName;
  private final BigDecimal plannedQty;
  private final BigDecimal consumedQty;
  private final BigDecimal missingQty;
  private final BigDecimal availableStock;

  public ConsumedProductResponse(
      Product product,
      BigDecimal plannedQty,
      BigDecimal consumedQty,
      BigDecimal missingQty,
      BigDecimal availableStock) {
    super(product.getVersion());
    this.productId = product.getId();
    this.productName = product.getName();
    this.plannedQty = plannedQty;
    this.consumedQty = consumedQty;
    this.missingQty = missingQty;
    this.availableStock = availableStock;
  }

  public Long getProductId() {
    return productId;
  }

  public String getProductName() {
    return productName;
  }

  public BigDecimal getPlannedQty() {
    return plannedQty;
  }

  public BigDecimal getConsumedQty() {
    return consumedQty;
  }

  public BigDecimal getMissingQty() {
    return missingQty;
  }

  public BigDecimal getAvailableStock() {
    return availableStock;
  }
}
