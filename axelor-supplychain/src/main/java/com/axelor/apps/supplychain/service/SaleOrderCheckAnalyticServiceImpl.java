/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import java.util.ArrayList;
import java.util.List;

public class SaleOrderCheckAnalyticServiceImpl implements SaleOrderCheckAnalyticService {

  @Override
  public void checkSaleOrderLinesAnalyticDistribution(SaleOrder saleOrder) throws AxelorException {

    List<String> productList = new ArrayList<>();
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      if (saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_NORMAL
          && saleOrderLine.getAnalyticDistributionTemplate() == null) {
        productList.add(saleOrderLine.getProductName());
      }
    }
    if (!productList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(SupplychainExceptionMessage.SALE_ORDER_ANALYTIC_DISTRIBUTION_ERROR),
          productList);
    }
  }
}
