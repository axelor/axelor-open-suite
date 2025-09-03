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

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SolDetailsDurationServiceImpl implements SolDetailsDurationService {

  protected final ProdProcessLineComputationService prodProcessLineComputationService;

  @Inject
  public SolDetailsDurationServiceImpl(
      ProdProcessLineComputationService prodProcessLineComputationService) {
    this.prodProcessLineComputationService = prodProcessLineComputationService;
  }

  @Override
  public BigDecimal computeSolDetailsDuration(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrderLine saleOrderLine) {
    BigDecimal nbCycle =
        prodProcessLineComputationService.computeNbCycle(
            saleOrderLine.getQtyToProduce(), saleOrderLineDetails.getMaxCapacityPerCycle());
    return prodProcessLineComputationService
        .computeMachineDuration(
            nbCycle,
            saleOrderLineDetails.getDurationPerCycle(),
            prodProcessLineComputationService.computeMachineInstallingDuration(
                nbCycle,
                saleOrderLineDetails.getStartingDuration(),
                saleOrderLineDetails.getEndingDuration(),
                saleOrderLineDetails.getSetupDuration()))
        .divide(BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }
}
