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
package com.axelor.apps.sale.rest.dto;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class ProductPostRequest extends RequestPostStructure {

  @NotNull @NotEmpty private List<ProductResquest> productList;

  private Long companyId;

  private Long partnerId;
  private Long currencyId;

  public List<ProductResquest> getProductList() {
    return productList;
  }

  public void setProductList(List<ProductResquest> productList) {
    this.productList = productList;
  }

  public void setPartnerId(Long partnerId) {
    this.partnerId = partnerId;
  }

  public Long getCurrencyId() {
    return currencyId;
  }

  public void setCurrencyId(Long currencyId) {
    this.currencyId = currencyId;
  }

  public Long getCompanyId() {
    return companyId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public Long getPartnerId() {
    return partnerId;
  }

  public Company fetchCompany() {
    if (companyId == null || companyId == 0L) {
      return null;
    }
    return ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION);
  }

  public Partner fetchPartner() {
    if (partnerId == null || partnerId == 0L) {
      return null;
    }
    return ObjectFinder.find(Partner.class, partnerId, ObjectFinder.NO_VERSION);
  }

  public Currency fetchCurrency() {
    if (currencyId == null || currencyId == 0L) {
      return null;
    }
    return ObjectFinder.find(Currency.class, currencyId, ObjectFinder.NO_VERSION);
  }
}
