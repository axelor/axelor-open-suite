package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class MassMovePutRequest extends RequestStructure {

  @NotNull
  @Min(1)
  private Integer statusSelect;

  @NotNull
  @Min(0)
  private Long companyId;

  @NotNull
  @Min(0)
  private Long cartStockLocationId;

  @NotNull
  @Min(0)
  private Long commonFromStockLocationId;

  @NotNull
  @Min(0)
  private Long commonToStockLocationId;

  public Integer getStatusSelect() {
    return statusSelect;
  }

  public void setStatusSelect(Integer statusSelect) {
    this.statusSelect = statusSelect;
  }

  public Long getCompanyId() {
    return companyId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public Long getCartStockLocationId() {
    return cartStockLocationId;
  }

  public void setCartStockLocationId(Long cartStockLocationId) {
    this.cartStockLocationId = cartStockLocationId;
  }

  public Long getCommonFromStockLocationId() {
    return commonFromStockLocationId;
  }

  public void setCommonFromStockLocationId(Long commonFromStockLocationId) {
    this.commonFromStockLocationId = commonFromStockLocationId;
  }

  public Long getCommonToStockLocationId() {
    return commonToStockLocationId;
  }

  public void setCommonToStockLocationId(Long commonToStockLocationId) {
    this.commonToStockLocationId = commonToStockLocationId;
  }

  // Transform id to object
  public Company fetchCompany() {
    return companyId != null
        ? ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION)
        : null;
  }

  public StockLocation fetchCarStockLocation() {
    return cartStockLocationId != null
        ? ObjectFinder.find(StockLocation.class, cartStockLocationId, ObjectFinder.NO_VERSION)
        : null;
  }

  public StockLocation fetchCommonFromStockLocation() {
    return commonFromStockLocationId != null
        ? ObjectFinder.find(StockLocation.class, commonFromStockLocationId, ObjectFinder.NO_VERSION)
        : null;
  }

  public StockLocation fetchCommonToStockLocation() {
    return commonToStockLocationId != null
        ? ObjectFinder.find(StockLocation.class, commonToStockLocationId, ObjectFinder.NO_VERSION)
        : null;
  }
}
