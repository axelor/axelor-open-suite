package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestPostStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StockTrackingNumberPostRequest extends RequestPostStructure {
  @NotNull
  @Min(0)
  private Long productId;

  @NotNull
  @Min(0)
  private Long companyId;

  private String notes;

  private String origin;

  public StockTrackingNumberPostRequest() {}

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Long getCompanyId() {
    return companyId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }

  // Transform id to object
  public Product fetchProduct() {
    return ObjectFinder.find(Product.class, productId, ObjectFinder.NO_VERSION);
  }

  public Company fetchCompany() {
    return ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION);
  }
}
