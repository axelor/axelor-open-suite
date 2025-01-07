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

import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.sale.db.SaleOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleOrderTaxNumberServiceImpl implements SaleOrderTaxNumberService {

  @Override
  public Map<String, Object> getTaxNumber(SaleOrder saleOrder) {
    Map<String, Object> saleOrderMap = new HashMap<>();
    Company company = saleOrder.getCompany();
    if (company != null) {
      List<TaxNumber> taxNumberList = company.getTaxNumberList();
      if (taxNumberList.size() == 1) {
        saleOrder.setTaxNumber(taxNumberList.stream().findFirst().orElse(null));
      }
      saleOrderMap.put("taxNumber", saleOrder.getTaxNumber());
    }

    return saleOrderMap;
  }
}
