package com.axelor.apps.supplychain.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PackagingPutRequest extends RequestStructure {

  @NotNull
  @Min(0)
  private Long packageUsedId;

  public Long getPackageUsedId() {
    return packageUsedId;
  }

  public void setPackageUsedId(Long packageUsedId) {
    this.packageUsedId = packageUsedId;
  }

  public Product fetchPackageUsed() {
    if (packageUsedId == null || packageUsedId == 0L) {
      return null;
    }
    return ObjectFinder.find(Product.class, packageUsedId, ObjectFinder.NO_VERSION);
  }
}
