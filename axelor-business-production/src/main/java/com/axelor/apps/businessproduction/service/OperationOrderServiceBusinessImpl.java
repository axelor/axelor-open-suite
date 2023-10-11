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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.operationorder.OperationOrderServiceImpl;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class OperationOrderServiceBusinessImpl extends OperationOrderServiceImpl {

  @Inject
  public OperationOrderServiceBusinessImpl(
      BarcodeGeneratorService barcodeGeneratorService,
      AppProductionService appProductionService,
      ManufOrderStockMoveService manufOrderStockMoveService) {
    super(barcodeGeneratorService, appProductionService, manufOrderStockMoveService);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder createOperationOrder(ManufOrder manufOrder, ProdProcessLine prodProcessLine)
      throws AxelorException {

    OperationOrder operationOrder = super.createOperationOrder(manufOrder, prodProcessLine);

    if (appProductionService.isApp("production")
        && Boolean.TRUE.equals(
            appProductionService.getAppProduction().getManageBusinessProduction())) {
      operationOrder.setIsToInvoice(manufOrder.getIsToInvoice());
    }

    return operationOrder;
  }
}
