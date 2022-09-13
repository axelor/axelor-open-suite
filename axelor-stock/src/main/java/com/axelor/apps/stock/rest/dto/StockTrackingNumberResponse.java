package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.tool.api.ResponseStructure;

public class StockTrackingNumberResponse extends ResponseStructure {

  private final Long id;
  private final Long productId;
  private final String trackingNumberSeq;
  private final String serialNumber;

  public StockTrackingNumberResponse(TrackingNumber trackingNumber) {
    super(trackingNumber.getVersion());
    this.id = trackingNumber.getId();
    this.productId = trackingNumber.getProduct().getId();
    this.trackingNumberSeq = trackingNumber.getTrackingNumberSeq();
    this.serialNumber = trackingNumber.getSerialNumber();
  }

  public Long getId() {
    return id;
  }

  public Long getProductId() {
    return productId;
  }

  public String getTrackingNumberSeq() {
    return trackingNumberSeq;
  }

  public String getSerialNumber() {
    return serialNumber;
  }
}
