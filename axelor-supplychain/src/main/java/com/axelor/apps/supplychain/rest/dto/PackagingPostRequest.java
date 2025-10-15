package com.axelor.apps.supplychain.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class PackagingPostRequest extends RequestPostStructure {

  @Min(0)
  private Long logisticalFormId;

  @Min(0)
  private Long parentPackagingId;

  @NotNull
  @Min(0)
  private Long packageUsedId;

  public Long getLogisticalFormId() {
    return logisticalFormId;
  }

  public void setLogisticalFormId(Long logisticalFormId) {
    this.logisticalFormId = logisticalFormId;
  }

  public Long getParentPackagingId() {
    return parentPackagingId;
  }

  public void setParentPackagingId(Long parentPackagingId) {
    this.parentPackagingId = parentPackagingId;
  }

  public Long getPackageUsedId() {
    return packageUsedId;
  }

  public void setPackageUsedId(Long packageUsedId) {
    this.packageUsedId = packageUsedId;
  }

  public LogisticalForm fetchLogisticalForm() {
    if (logisticalFormId == null || logisticalFormId == 0L) {
      return null;
    }
    return ObjectFinder.find(LogisticalForm.class, logisticalFormId, ObjectFinder.NO_VERSION);
  }

  public Packaging fetchPackaging() {
    if (parentPackagingId == null || parentPackagingId == 0L) {
      return null;
    }
    return ObjectFinder.find(Packaging.class, parentPackagingId, ObjectFinder.NO_VERSION);
  }

  public Product fetchPackageUsed() {
    if (packageUsedId == null || packageUsedId == 0L) {
      return null;
    }
    return ObjectFinder.find(Product.class, packageUsedId, ObjectFinder.NO_VERSION);
  }
}
