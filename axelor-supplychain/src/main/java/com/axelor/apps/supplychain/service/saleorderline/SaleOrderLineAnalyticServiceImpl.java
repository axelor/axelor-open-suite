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
package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.service.analytic.AnalyticAxisService;
import com.axelor.apps.account.service.analytic.AnalyticGroupService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.Map;
import java.util.stream.Collectors;

public class SaleOrderLineAnalyticServiceImpl implements SaleOrderLineAnalyticService {

  protected AnalyticGroupService analyticGroupService;
  protected AnalyticAxisService analyticAxisService;

  @Inject
  public SaleOrderLineAnalyticServiceImpl(
      AnalyticGroupService analyticGroupService, AnalyticAxisService analyticAxisService) {
    this.analyticGroupService = analyticGroupService;
    this.analyticAxisService = analyticAxisService;
  }

  @Override
  public Map<String, Object> printAnalyticAccounts(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
    return analyticGroupService.getAnalyticAccountValueMap(
        analyticLineModel, saleOrder.getCompany());
  }

  @Override
  public void checkAnalyticAxisByCompany(SaleOrder saleOrder) throws AxelorException {
    if (saleOrder == null || ObjectUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      return;
    }

    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      if (!ObjectUtils.isEmpty(saleOrderLine.getAnalyticMoveLineList())) {
        analyticAxisService.checkRequiredAxisByCompany(
            saleOrder.getCompany(),
            saleOrderLine.getAnalyticMoveLineList().stream()
                .map(AnalyticMoveLine::getAnalyticAxis)
                .collect(Collectors.toList()));
      }
    }
  }
}
