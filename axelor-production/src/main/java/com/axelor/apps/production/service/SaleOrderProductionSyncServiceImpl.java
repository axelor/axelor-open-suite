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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.Objects;

public class SaleOrderProductionSyncServiceImpl implements SaleOrderProductionSyncService {

  protected final SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService;
  protected final SaleOrderLineBomService saleOrderLineBomService;
  protected final SaleOrderLineDetailsBomService saleOrderLineDetailsBomService;
  protected final SolBomCustomizationService solBomCustomizationService;
  protected final SolDetailsBomUpdateService solDetailsBomUpdateService;
  protected final SolBomUpdateService solBomUpdateService;
  protected final AppProductionService appProductionService;

  @Inject
  public SaleOrderProductionSyncServiceImpl(
      SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService,
      SaleOrderLineBomService saleOrderLineBomService,
      SaleOrderLineDetailsBomService saleOrderLineDetailsBomService,
      SolBomCustomizationService solBomCustomizationService,
      SolDetailsBomUpdateService solDetailsBomUpdateService,
      SolBomUpdateService solBomUpdateService,
      AppProductionService appProductionService) {
    this.saleOrderLineBomLineMappingService = saleOrderLineBomLineMappingService;
    this.saleOrderLineBomService = saleOrderLineBomService;
    this.saleOrderLineDetailsBomService = saleOrderLineDetailsBomService;
    this.solBomCustomizationService = solBomCustomizationService;
    this.solDetailsBomUpdateService = solDetailsBomUpdateService;
    this.solBomUpdateService = solBomUpdateService;
    this.appProductionService = appProductionService;
  }

  @Override
  public void syncSaleOrderLineList(SaleOrder saleOrder) throws AxelorException {
    Objects.requireNonNull(saleOrder);

    if (!appProductionService.getAppProduction().getAllowPersonalizedBOM()) {
      return;
    }

    if (saleOrder.getSaleOrderLineList() != null) {
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        syncSaleOrderLine(saleOrderLine);
      }
    }
  }

  protected void syncSaleOrderLine(SaleOrderLine saleOrderLine) throws AxelorException {
    Objects.requireNonNull(saleOrderLine);
    // No personalized BOM = no synchronization
    if (!appProductionService.getAppProduction().getAllowPersonalizedBOM()) {
      return;
    }
    if (!saleOrderLine.getIsToProduce()) {
      return;
    }
    // First we sync sub lines, because if a change occurs is one of them
    // We take it into account when sync the current sale order line
    if (saleOrderLine.getSubSaleOrderLineList() != null) {
      for (SaleOrderLine subSaleOrderLine : saleOrderLine.getSubSaleOrderLineList()) {
        syncSaleOrderLine(subSaleOrderLine);
      }
    }

    // if bom lines list is same size as sub line list (checking if more line or less)
    // and if each lines are sync
    var isUpdated = solBomUpdateService.isUpdated(saleOrderLine);
    var isSolDetailsUpdated = solDetailsBomUpdateService.isSolDetailsUpdated(saleOrderLine);

    if (isUpdated && isSolDetailsUpdated) {
      return;
    }

    if (!isUpdated || !isSolDetailsUpdated) {
      if (!saleOrderLine.getBillOfMaterial().getPersonalized()) {
        solBomCustomizationService.customizeBomOf(saleOrderLine);
      } else {
        if (!isUpdated) {
          solBomUpdateService.updateSolWithBillOfMaterial(saleOrderLine);
        }
        if (!isSolDetailsUpdated) {
          solDetailsBomUpdateService.updateSolDetailslWithBillOfMaterial(saleOrderLine);
        }
      }
    }
  }
}
