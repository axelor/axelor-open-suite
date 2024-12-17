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
package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.studio.db.AppSupplychain;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderIntercoServiceImpl implements SaleOrderIntercoService {

  protected AppSupplychainService appSupplychainService;
  protected CompanyRepository companyRepository;

  @Inject
  public SaleOrderIntercoServiceImpl(
      AppSupplychainService appSupplychainService, CompanyRepository companyRepository) {
    this.appSupplychainService = appSupplychainService;
    this.companyRepository = companyRepository;
  }

  @Override
  public Map<String, Object> getInterco(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();
    boolean isIntercoFromSale = appSupplychain.getIntercoFromSale();
    boolean createdByInterco = saleOrder.getCreatedByInterco();
    Partner clientPartner = saleOrder.getClientPartner();
    Company company =
        companyRepository
            .all()
            .filter("self.partner = :clientPartner")
            .bind("clientPartner", clientPartner)
            .fetchOne();
    boolean isInterco =
        isIntercoFromSale && !createdByInterco && clientPartner != null && company != null;
    saleOrder.setInterco(isInterco);
    saleOrderMap.put("interco", saleOrder.getInterco());
    return saleOrderMap;
  }
}
