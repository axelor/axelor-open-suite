package com.axelor.apps.production.rest.dto;

import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.tool.api.ResponseStructure;

public class TrackingNumberResponse extends ResponseStructure {
  private final Long trackingNumberId;
  private final String trackingNumberSeq;

  public TrackingNumberResponse(TrackingNumber trackingNumber) {
    super(trackingNumber.getVersion());
    this.trackingNumberId = trackingNumber.getId();
    this.trackingNumberSeq = trackingNumber.getTrackingNumberSeq();
  }

  public Long getTrackingNumberId() {
    return trackingNumberId;
  }

  public String getTrackingNumberSeq() {
    return trackingNumberSeq;
  }
}
