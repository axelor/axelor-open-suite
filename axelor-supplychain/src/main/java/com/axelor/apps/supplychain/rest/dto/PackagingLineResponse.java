package com.axelor.apps.supplychain.rest.dto;

import com.axelor.apps.supplychain.db.PackagingLine;
import com.axelor.utils.api.ResponseStructure;

public class PackagingLineResponse extends ResponseStructure {

  private Long packagingLineId;

  public PackagingLineResponse(PackagingLine packagingLine) {
    super(packagingLine.getVersion());
    this.packagingLineId = packagingLine.getId();
  }

  public Long getPackagingLineId() {
    return packagingLineId;
  }
}
