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
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.util.Objects;

public class SolDetailsProdProcessLineMappingServiceImpl
    implements SolDetailsProdProcessLineMappingService {

  protected final AppBaseService appBaseService;
  protected final AppProductionService appProductionService;
  protected final SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService;
  protected final SolDetailsProdProcessComputeQtyService solDetailsProdProcessComputeQtyService;

  @Inject
  public SolDetailsProdProcessLineMappingServiceImpl(
      AppBaseService appBaseService,
      AppProductionService appProductionService,
      SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService,
      SolDetailsProdProcessComputeQtyService solDetailsProdProcessComputeQtyService) {
    this.appBaseService = appBaseService;
    this.appProductionService = appProductionService;
    this.saleOrderLineDetailsPriceService = saleOrderLineDetailsPriceService;
    this.solDetailsProdProcessComputeQtyService = solDetailsProdProcessComputeQtyService;
  }

  @Override
  public SaleOrderLineDetails mapToSaleOrderLineDetails(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, ProdProcessLine prodProcessLine)
      throws AxelorException {
    Objects.requireNonNull(prodProcessLine);

    SaleOrderLineDetails saleOrderLineDetails = getDefaultOperationSolDetails(prodProcessLine);
    solDetailsProdProcessComputeQtyService.setQty(
        saleOrderLine, prodProcessLine, saleOrderLineDetails);
    saleOrderLineDetailsPriceService.computePrices(saleOrderLineDetails, saleOrder, saleOrderLine);
    setUnit(saleOrderLineDetails);
    setProdProcessLineInfo(saleOrderLineDetails, prodProcessLine);

    return saleOrderLineDetails;
  }

  protected SaleOrderLineDetails getDefaultOperationSolDetails(ProdProcessLine prodProcessLine) {
    SaleOrderLineDetails saleOrderLineDetails = new SaleOrderLineDetails();
    saleOrderLineDetails.setProdProcessLine(prodProcessLine);
    saleOrderLineDetails.setTypeSelect(SaleOrderLineDetailsRepository.TYPE_OPERATION);
    saleOrderLineDetails.setTitle(prodProcessLine.getName());
    return saleOrderLineDetails;
  }

  protected void setUnit(SaleOrderLineDetails saleOrderLineDetails) {
    AppBase appBase = appBaseService.getAppBase();
    saleOrderLineDetails.setUnit(appBase.getUnitHours());
  }

  protected void setProdProcessLineInfo(
      SaleOrderLineDetails saleOrderLineDetails, ProdProcessLine prodProcessLine) {
    saleOrderLineDetails.setMinCapacityPerCycle(prodProcessLine.getMinCapacityPerCycle());
    saleOrderLineDetails.setMaxCapacityPerCycle(prodProcessLine.getMaxCapacityPerCycle());
    saleOrderLineDetails.setDurationPerCycle(prodProcessLine.getDurationPerCycle());
    saleOrderLineDetails.setSetupDuration(prodProcessLine.getSetupDuration());
    saleOrderLineDetails.setStartingDuration(prodProcessLine.getStartingDuration());
    saleOrderLineDetails.setEndingDuration(prodProcessLine.getEndingDuration());
    saleOrderLineDetails.setHumanDuration(prodProcessLine.getHumanDuration());
    saleOrderLineDetails.setCostTypeSelect(prodProcessLine.getCostTypeSelect());
    saleOrderLineDetails.setCostAmount(prodProcessLine.getCostAmount());
    saleOrderLineDetails.setHrCostTypeSelect(prodProcessLine.getHrCostTypeSelect());
    saleOrderLineDetails.setHrCostAmount(prodProcessLine.getHrCostAmount());
  }
}
