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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.inject.Beans;

public class SaleOrderDateServiceImpl implements SaleOrderDateService {

  @Override
  public SaleOrder computeEndOfValidityDate(SaleOrder saleOrder) {
    Company company = saleOrder.getCompany();
    if (saleOrder.getDuration() == null && company != null && company.getSaleConfig() != null) {
      saleOrder.setDuration(company.getSaleConfig().getDefaultValidityDuration());
    }
    if (saleOrder.getCreationDate() != null) {
      saleOrder.setEndOfValidityDate(
          Beans.get(DurationService.class)
              .computeDuration(saleOrder.getDuration(), saleOrder.getCreationDate()));
    }
    return saleOrder;
  }
}
