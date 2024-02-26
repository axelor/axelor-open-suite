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
package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.service.ProdProcessLineOutsourceService;
import com.axelor.apps.production.service.manuforder.ManufOrderOutsourceService;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class OperationOrderOutsourceServiceImpl implements OperationOrderOutsourceService {

  protected ProdProcessLineOutsourceService prodProcessLineOutsourceService;
  protected ManufOrderOutsourceService manufOrderOutsourceService;

  @Inject
  public OperationOrderOutsourceServiceImpl(
      ProdProcessLineOutsourceService prodProcessLineOutsourceService,
      ManufOrderOutsourceService manufOrderOutsourceService) {
    this.prodProcessLineOutsourceService = prodProcessLineOutsourceService;
    this.manufOrderOutsourceService = manufOrderOutsourceService;
  }

  @Override
  public Optional<Partner> getOutsourcePartner(OperationOrder operationOrder) {
    Objects.requireNonNull(operationOrder);
    Objects.requireNonNull(operationOrder.getManufOrder());

    // Fetching from manufOrder
    if (operationOrder.getOutsourcing() && operationOrder.getManufOrder().getOutsourcing()) {
      return manufOrderOutsourceService.getOutsourcePartner(operationOrder.getManufOrder());
      // Fetching from prodProcessLine or itself
    } else if (operationOrder.getOutsourcing()
        && !operationOrder.getManufOrder().getOutsourcing()) {
      ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();
      if ((prodProcessLine.getOutsourcing() || prodProcessLine.getOutsourcable())
          && operationOrder.getOutsourcingPartner() == null) {
        return prodProcessLineOutsourceService.getOutsourcePartner(prodProcessLine);
      } else {
        return Optional.ofNullable(operationOrder.getOutsourcingPartner());
      }
    }
    return Optional.empty();
  }

  @Override
  public boolean getUseLineInGeneratedPO(OperationOrder operationOrder) {
    Objects.requireNonNull(operationOrder);
    Objects.requireNonNull(operationOrder.getProdProcessLine());

    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();

    if (operationOrder.getManufOrder().getOutsourcing()
        || prodProcessLine.getOutsourcing()
        || operationOrder.getOutsourcing()
        || (prodProcessLine.getOutsourcable() && operationOrder.getOutsourcing())) {
      return prodProcessLine.getUseLineInGeneratedPurchaseOrder();
    }
    return false;
  }
}
