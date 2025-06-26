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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SolDetailsProdProcessComputeQtyServiceImpl
    implements SolDetailsProdProcessComputeQtyService {

  protected final AppBaseService appBaseService;
  protected final ProdProcessLineComputationService prodProcessLineComputationService;

  @Inject
  public SolDetailsProdProcessComputeQtyServiceImpl(
      AppBaseService appBaseService,
      ProdProcessLineComputationService prodProcessLineComputationService) {
    this.appBaseService = appBaseService;
    this.prodProcessLineComputationService = prodProcessLineComputationService;
  }

  @Override
  public void setQty(
      SaleOrderLine saleOrderLine,
      ProdProcessLine prodProcessLine,
      SaleOrderLineDetails saleOrderLineDetails)
      throws AxelorException {
    int nbDecimalForQty = appBaseService.getNbDecimalDigitForQty();
    WorkCenter workCenter = prodProcessLine.getWorkCenter();
    BigDecimal nbCycle =
        prodProcessLineComputationService.getNbCycle(
            prodProcessLine, saleOrderLine.getQtyToProduce());
    int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();
    switch (workCenterTypeSelect) {
      case WorkCenterRepository.WORK_CENTER_TYPE_HUMAN:
        saleOrderLineDetails.setQty(
            prodProcessLineComputationService
                .getHourHumanDuration(prodProcessLine, nbCycle)
                .setScale(nbDecimalForQty, RoundingMode.HALF_UP));
        break;
      case WorkCenterRepository.WORK_CENTER_TYPE_MACHINE:
        saleOrderLineDetails.setQty(
            prodProcessLineComputationService
                .getHourMachineDuration(prodProcessLine, nbCycle)
                .setScale(nbDecimalForQty, RoundingMode.HALF_UP));
        break;
      default:
        saleOrderLineDetails.setQty(
            prodProcessLineComputationService
                .getHourTotalDuration(prodProcessLine, nbCycle)
                .setScale(nbDecimalForQty, RoundingMode.HALF_UP));
    }
  }
}
