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

import com.axelor.apps.account.service.analytic.AnalyticGroupService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineAnalyticServiceImpl implements SaleOrderLineAnalyticService {

  protected AnalyticGroupService analyticGroupService;
  protected AnalyticLineModelService analyticLineModelService;

  @Inject
  public SaleOrderLineAnalyticServiceImpl(
      AnalyticGroupService analyticGroupService,
      AnalyticLineModelService analyticLineModelService) {
    this.analyticGroupService = analyticGroupService;
    this.analyticLineModelService = analyticLineModelService;
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
      AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
      analyticLineModelService.checkRequiredAxisByCompany(analyticLineModel);
    }
  }
}
