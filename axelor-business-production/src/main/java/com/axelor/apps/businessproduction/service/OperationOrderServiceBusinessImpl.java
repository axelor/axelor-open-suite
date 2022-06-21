/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.operationorder.OperationOrderServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationOrderServiceBusinessImpl extends OperationOrderServiceImpl {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder createOperationOrder(ManufOrder manufOrder, ProdProcessLine prodProcessLine)
      throws AxelorException {
    AppProductionService appProductionService = Beans.get(AppProductionService.class);
    if (!appProductionService.isApp("production")
        || !appProductionService.getAppProduction().getManageBusinessProduction()) {
      return super.createOperationOrder(manufOrder, prodProcessLine);
    }

    OperationOrder operationOrder =
        this.createOperationOrder(
            manufOrder,
            prodProcessLine.getPriority(),
            manufOrder.getIsToInvoice(),
            prodProcessLine.getWorkCenter(),
            prodProcessLine.getWorkCenter(),
            prodProcessLine);

    return Beans.get(OperationOrderRepository.class).save(operationOrder);
  }

  @Transactional
  public OperationOrder createOperationOrder(
      ManufOrder manufOrder,
      int priority,
      boolean isToInvoice,
      WorkCenter workCenter,
      WorkCenter machineWorkCenter,
      ProdProcessLine prodProcessLine) {

    logger.debug(
        "Création d'une opération {} pour l'OF {}", priority, manufOrder.getManufOrderSeq());

    String operationName = prodProcessLine.getName();

    OperationOrder operationOrder =
        new OperationOrder(
            priority,
            this.computeName(manufOrder, priority, operationName),
            operationName,
            manufOrder,
            workCenter,
            machineWorkCenter,
            OperationOrderRepository.STATUS_DRAFT,
            prodProcessLine);

    operationOrder.setIsToInvoice(isToInvoice);

    this._createHumanResourceList(operationOrder, machineWorkCenter);

    return Beans.get(OperationOrderRepository.class).save(operationOrder);
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
