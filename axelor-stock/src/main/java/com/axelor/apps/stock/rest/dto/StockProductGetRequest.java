package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestStructure;
import java.util.Objects;
import javax.validation.constraints.Min;

public class StockProductGetRequest extends RequestStructure {

  @Min(0)
  private Long companyId;

  @Min(0)
  private Long stockLocationId;

  public StockProductGetRequest() {}

  public Long getCompanyId() {
    return companyId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public Long getStockLocationId() {
    return stockLocationId;
  }

  public void setStockLocationId(Long stockLocationId) {
    this.stockLocationId = stockLocationId;
  }

  // Transform id to object
  public Company getCompany() {
    if (companyId == null) {
      return null;
    }
    return ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION);
  }

  public StockLocation getStockLocation() {
    if (stockLocationId == null) {
      return null;
    }
    StockLocation stockLocation =
        ObjectFinder.find(StockLocation.class, stockLocationId, ObjectFinder.NO_VERSION);
    if (!Objects.equals(stockLocation.getCompany().getId(), companyId)) {
      return null;
    }
    return stockLocation;
  }
}
