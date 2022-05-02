package com.axelor.apps.supplychain.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.tool.api.ApiStructure;
import java.math.BigDecimal;
import java.util.Map;

public class StockProductResponse implements ApiStructure {

  private final long id;
  private final BigDecimal realQty;
  private final BigDecimal futureQty;
  private final BigDecimal allocatedQty;
  private final BigDecimal saleOrderQty;
  private final BigDecimal purchaseOrderQty;
  private final BigDecimal availableStock;
  private final BigDecimal buildingQty;
  private final BigDecimal consumeManufOrderQty;
  private final BigDecimal missingManufOrderQty;

  public StockProductResponse(Product product, Map<String, Object> qtys) {
    this.id = product.getId();
    this.realQty = (BigDecimal) qtys.get("$realQty");
    this.futureQty = (BigDecimal) qtys.get("$futureQty");
    this.allocatedQty = (BigDecimal) qtys.get("$reservedQty");
    this.saleOrderQty = (BigDecimal) qtys.get("$saleOrderQty");
    this.purchaseOrderQty = (BigDecimal) qtys.get("$purchaseOrderQty");
    this.availableStock = (BigDecimal) qtys.get("$availableQty");
    this.buildingQty = (BigDecimal) qtys.get("$buildingQty");
    this.consumeManufOrderQty = (BigDecimal) qtys.get("$consumeManufOrderQty");
    this.missingManufOrderQty = (BigDecimal) qtys.get("$missingManufOrderQty");
  }

  public String getObjectName() {
    return "product";
  }

  public long getId() {
    return id;
  }

  public BigDecimal getRealQty() {
    return realQty;
  }

  public BigDecimal getFutureQty() {
    return futureQty;
  }

  public BigDecimal getAllocatedQty() {
    return allocatedQty;
  }

  public BigDecimal getSaleOrderQty() {
    return saleOrderQty;
  }

  public BigDecimal getPurchaseOrderQty() {
    return purchaseOrderQty;
  }

  public BigDecimal getAvailableStock() {
    return availableStock;
  }

  public BigDecimal getBuildingQty() {
    return buildingQty;
  }

  public BigDecimal getConsumeManufOrderQty() {
    return consumeManufOrderQty;
  }

  public BigDecimal getMissingManufOrderQty() {
    return missingManufOrderQty;
  }
}
