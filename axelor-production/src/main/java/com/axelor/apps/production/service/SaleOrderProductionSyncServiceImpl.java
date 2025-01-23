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
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.Objects;

public class SaleOrderProductionSyncServiceImpl implements SaleOrderProductionSyncService {

  protected final SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService;
  protected final SaleOrderLineBomService saleOrderLineBomService;
  protected final AppProductionService appProductionService;

  @Inject
  public SaleOrderProductionSyncServiceImpl(
      SaleOrderLineBomService saleOrderLineBomService,
      SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService,
      AppProductionService appProductionService) {
    this.saleOrderLineBomLineMappingService = saleOrderLineBomLineMappingService;
    this.saleOrderLineBomService = saleOrderLineBomService;
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
    var isUpdated = saleOrderLineBomService.isUpdated(saleOrderLine);

    if (isUpdated) {
      return;
    }

    // Not sync
    // Checking first if a personalized bom is created on saleOrderLine. If not, will create one.
    if (!saleOrderLine.getBillOfMaterial().getPersonalized()) {
      saleOrderLineBomService.customizeBomOf(saleOrderLine);
    }
    // Will sync with current personalized bom
    else {
      saleOrderLineBomService.updateWithBillOfMaterial(saleOrderLine);
    }
  }
}
