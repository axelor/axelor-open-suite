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
package com.axelor.apps.production.service;

import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderBlockingSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderCheckBlockingSupplychainServiceImpl;
import com.google.inject.Inject;
import java.util.List;

public class SaleOrderCheckBlockingServiceProductionServiceImpl
    extends SaleOrderCheckBlockingSupplychainServiceImpl {

  protected final SaleOrderBlockingProductionService saleOrderBlockingProductionService;
  protected final AppProductionService appProductionService;

  @Inject
  public SaleOrderCheckBlockingServiceProductionServiceImpl(
      SaleOrderBlockingSupplychainService saleOrderBlockingSupplychainService,
      AppSupplychainService appSupplychainService,
      SaleOrderBlockingProductionService saleOrderBlockingProductionService,
      AppProductionService appProductionService) {
    super(saleOrderBlockingSupplychainService, appSupplychainService);
    this.saleOrderBlockingProductionService = saleOrderBlockingProductionService;
    this.appProductionService = appProductionService;
  }

  @Override
  public List<String> checkBlocking(SaleOrder saleOrder) {
    var alertList = super.checkBlocking(saleOrder);

    if (saleOrderBlockingProductionService.hasOnGoingBlocking(saleOrder)
        && appProductionService.getAppProduction().getProductionOrderGenerationAuto()) {
      alertList.add(ProductionExceptionMessage.SALE_ORDER_LINES_CANNOT_PRODUCT);
    }

    return alertList;
  }
}
