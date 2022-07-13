package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestPostStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class InventoryLinePostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long inventoryId;

  @NotNull
  @Min(0)
  private Integer inventoryVersion;

  @NotNull
  @Min(0)
  private Long productId;

  @Min(0)
  private Long trackingNumberId;

  private String rack;

  @NotNull
  @Min(0)
  private BigDecimal realQty;

  public InventoryLinePostRequest() {}

  public Long getInventoryId() {
    return inventoryId;
  }

  public void setInventoryId(Long inventoryId) {
    this.inventoryId = inventoryId;
  }

  public void setInventoryVersion(Integer inventoryVersion) {
    this.inventoryVersion = inventoryVersion;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Long getTrackingNumberId() {
    return trackingNumberId;
  }

  public void setTrackingNumberId(Long trackingNumberId) {
    this.trackingNumberId = trackingNumberId;
  }

  public String getRack() {
    return rack;
  }

  public void setRack(String rack) {
    this.rack = rack;
  }

  public BigDecimal getRealQty() {
    return realQty;
  }

  public void setRealQty(BigDecimal realQty) {
    this.realQty = realQty;
  }

  // Transform id to object
  public Product fetchProduct() {
    return ObjectFinder.find(Product.class, productId, ObjectFinder.NO_VERSION);
  }

  public Inventory fetchInventory() {
    return ObjectFinder.find(Inventory.class, inventoryId, inventoryVersion);
  }

  public TrackingNumber fetchTrackingNumber() {
    if (this.trackingNumberId != null) {
      return ObjectFinder.find(TrackingNumber.class, trackingNumberId, ObjectFinder.NO_VERSION);
    } else {
      return null;
    }
  }
}
