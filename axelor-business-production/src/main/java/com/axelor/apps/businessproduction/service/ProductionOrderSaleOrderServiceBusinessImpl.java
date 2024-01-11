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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderServiceImpl;
import com.axelor.apps.production.service.productionorder.ProductionOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;

public class ProductionOrderSaleOrderServiceBusinessImpl
    extends ProductionOrderSaleOrderServiceImpl {

  @Inject
  public ProductionOrderSaleOrderServiceBusinessImpl(
      UnitConversionService unitConversionService,
      ProductionOrderService productionOrderService,
      ProductionOrderRepository productionOrderRepo,
      AppProductionService appProductionService) {
    super(unitConversionService, productionOrderService, productionOrderRepo, appProductionService);
  }

  @Override
  protected ProductionOrder createProductionOrder(SaleOrder saleOrder) throws AxelorException {

    ProductionOrder productionOrder = super.createProductionOrder(saleOrder);

    if (appProductionService.isApp("production")
        && appProductionService.getAppProduction().getManageBusinessProduction()) {
      productionOrder.setProject(saleOrder.getProject());
    }
    return productionOrder;
  }
}
