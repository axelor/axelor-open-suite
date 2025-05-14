/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.rest.dto;

import com.axelor.apps.base.db.Company;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.util.List;
import javax.validation.constraints.Min;

public class PurchaseRequestPostRequest extends RequestPostStructure {

  @Min(0)
  private Long companyId;

  @Min(0)
  private Integer status;

  private String description;

  private List<PurchaseRequestLineRequest> purchaseRequestLineList;

  public Long getCompanyId() {
    return companyId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<PurchaseRequestLineRequest> getPurchaseRequestLineList() {
    return purchaseRequestLineList;
  }

  public void setPurchaseRequestLineList(List<PurchaseRequestLineRequest> purchaseRequestLineList) {
    this.purchaseRequestLineList = purchaseRequestLineList;
  }

  public Company fetchCompany() {
    if (companyId == null || companyId == 0L) {
      return null;
    }
    return ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION);
  }
}
