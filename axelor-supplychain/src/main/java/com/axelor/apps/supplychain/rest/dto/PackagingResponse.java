package com.axelor.apps.supplychain.rest.dto;

import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.utils.api.ResponseStructure;

public class PackagingResponse extends ResponseStructure {

  private Long packagingId;

  public PackagingResponse(Packaging packaging) {
    super(packaging.getVersion());
    this.packagingId = packaging.getId();
  }

  public Long getPackagingId() {
    return packagingId;
  }
}
