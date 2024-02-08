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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.pricing.PricingGenericServiceImpl;
import com.axelor.apps.base.service.pricing.PricingObserver;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.google.inject.Inject;
import java.util.List;

public class SalePricingGenericServiceImpl extends PricingGenericServiceImpl {

  @Inject
  public SalePricingGenericServiceImpl(
      PricingService pricingService,
      PricingObserver pricingObserver,
      AppBaseService appBaseService) {
    super(pricingService, pricingObserver, appBaseService);
  }

  @Override
  public void computePricingsOnChildren(Company company, Model model) throws AxelorException {
    super.computePricingsOnChildren(company, model);

    if (SaleOrder.class.equals(EntityHelper.getEntityClass(model))) {
      SaleOrder saleOrder = (SaleOrder) model;
      if (!ObjectUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
        for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
          computePricingsOnModel(company, saleOrderLine);
        }
      }
    }
  }

  @Override
  public String updatePricingScaleLogs(List<StringBuilder> logsList, Model model) {
    String logs = super.updatePricingScaleLogs(logsList, model);

    if (SaleOrderLine.class.equals(EntityHelper.getEntityClass(model))) {
      SaleOrderLine saleOrderLine = (SaleOrderLine) model;
      saleOrderLine.setPricingScaleLogs(logs);
    }

    return logs;
  }
}
