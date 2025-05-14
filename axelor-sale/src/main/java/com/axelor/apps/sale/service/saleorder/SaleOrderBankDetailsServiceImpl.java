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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderBankDetailsServiceImpl implements SaleOrderBankDetailsService {

  protected BankDetailsService bankDetailsService;

  @Inject
  public SaleOrderBankDetailsServiceImpl(BankDetailsService bankDetailsService) {
    this.bankDetailsService = bankDetailsService;
  }

  @Override
  public Map<String, Object> getBankDetails(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    Company company = saleOrder.getCompany();
    if (company != null) {
      saleOrder.setCompanyBankDetails(
          bankDetailsService.getDefaultCompanyBankDetails(
              saleOrder.getCompany(), null, saleOrder.getClientPartner(), null));
      saleOrderMap.put("companyBankDetails", saleOrder.getCompanyBankDetails());
    }

    return saleOrderMap;
  }
}
