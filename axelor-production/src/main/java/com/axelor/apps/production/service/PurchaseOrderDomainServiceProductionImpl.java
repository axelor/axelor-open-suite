/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.PurchaseOrderDomainServiceImpl;

public class PurchaseOrderDomainServiceProductionImpl extends PurchaseOrderDomainServiceImpl {

  @Override
  public String getPartnerBaseDomain(Company company, PurchaseOrder purchaseOrder) {
    if (Boolean.TRUE.equals(purchaseOrder.getOutsourcingOrder())) {
      long companyId = company.getPartner() == null ? 0L : company.getPartner().getId();
      return String.format(
          "self.id != %d AND self.isContact = false AND self.isSubcontractor = true AND :company member of self.companySet",
          companyId);
    }
    return super.getPartnerBaseDomain(company, purchaseOrder);
  }
}
