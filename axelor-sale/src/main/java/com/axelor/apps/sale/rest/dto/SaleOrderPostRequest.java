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
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class SaleOrderPostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long clientPartnerId;

  private Long companyId;
  private Long contactId;
  private Long currencyId;
  private Boolean inAti;
  private List<SaleOrderLinePostRequest> saleOrderLineList;

  public List<SaleOrderLinePostRequest> getSaleOrderLineList() {
    return saleOrderLineList;
  }

  public void setSaleOrderLineList(List<SaleOrderLinePostRequest> saleOrderLineList) {
    this.saleOrderLineList = saleOrderLineList;
  }

  public Long getCompanyId() {
    return companyId;
  }

  public Long getContactId() {
    return contactId;
  }

  public Long getCurrencyId() {
    return currencyId;
  }

  public Long getClientPartnerId() {
    return clientPartnerId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public void setContactId(Long contactId) {
    this.contactId = contactId;
  }

  public void setCurrencyId(Long currencyId) {
    this.currencyId = currencyId;
  }

  public void setClientPartnerId(Long clientPartnerId) {
    this.clientPartnerId = clientPartnerId;
  }

  public Boolean getInAti() {
    return inAti;
  }

  public void setInAti(Boolean inAti) {
    this.inAti = inAti;
  }

  public Partner fetchClientPartner() {
    if (clientPartnerId == null || clientPartnerId == 0L) {
      return null;
    }
    return ObjectFinder.find(Partner.class, clientPartnerId, ObjectFinder.NO_VERSION);
  }

  public Partner fetchContact() {
    if (contactId == null || contactId == 0L) {
      return null;
    }
    return ObjectFinder.find(Partner.class, contactId, ObjectFinder.NO_VERSION);
  }

  public Company fetchCompany() {
    if (companyId == null || companyId == 0L) {
      return null;
    }
    return ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION);
  }

  public Currency fetchCurrency() {
    if (currencyId == null || currencyId == 0L) {
      return null;
    }
    return ObjectFinder.find(Currency.class, currencyId, ObjectFinder.NO_VERSION);
  }
}
