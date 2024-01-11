/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
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
  public Company fetchCompany() {
    if (companyId == null) {
      return null;
    }
    return ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION);
  }

  public StockLocation fetchStockLocation() {
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
