package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.TrackingNumber;
import java.math.BigDecimal;

public class StockMoveLinePostDTO {

  private Product product;

  private Unit unit;

  private TrackingNumber trackingNumber;

  private BigDecimal expectedQty;

  private BigDecimal realQty;

  private Integer conformity;

  private StockLocation fromStockLocation;

  private StockLocation toStockLocation;

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product product) {
    this.product = product;
  }

  public Unit getUnit() {
    return unit;
  }

  public void setUnit(Unit unit) {
    this.unit = unit;
  }

  public TrackingNumber getTrackingNumber() {
    return trackingNumber;
  }

  public void setTrackingNumber(TrackingNumber trackingNumber) {
    this.trackingNumber = trackingNumber;
  }

  public BigDecimal getExpectedQty() {
    return expectedQty;
  }

  public void setExpectedQty(BigDecimal expectedQty) {
    this.expectedQty = expectedQty;
  }

  public BigDecimal getRealQty() {
    return realQty;
  }

  public void setRealQty(BigDecimal realQty) {
    this.realQty = realQty;
  }

  public Integer getConformity() {
    return conformity;
  }

  public void setConformity(Integer conformity) {
    this.conformity = conformity;
  }

  public StockLocation getFromStockLocation() {
    return fromStockLocation;
  }

  public void setFromStockLocation(StockLocation fromStockLocation) {
    this.fromStockLocation = fromStockLocation;
  }

  public StockLocation getToStockLocation() {
    return toStockLocation;
  }

  public void setToStockLocation(StockLocation toStockLocation) {
    this.toStockLocation = toStockLocation;
  }
}
