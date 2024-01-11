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
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.operationorder.OperationOrderServiceImpl;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class OperationOrderServiceBusinessImpl extends OperationOrderServiceImpl {

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

  @Override
  protected ProdHumanResource copyProdHumanResource(ProdHumanResource prodHumanResource) {
    AppProductionService appProductionService = Beans.get(AppProductionService.class);

    if (!appProductionService.isApp("production")
        || !appProductionService.getAppProduction().getManageBusinessProduction()) {
      return super.copyProdHumanResource(prodHumanResource);
    }

    ProdHumanResource prodHumanResourceCopy =
        new ProdHumanResource(prodHumanResource.getProduct(), prodHumanResource.getDuration());
    prodHumanResourceCopy.setEmployee(prodHumanResource.getEmployee());
    return prodHumanResourceCopy;
  }
}
