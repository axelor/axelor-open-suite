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
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.studio.db.AppProduction;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;

public abstract class SaleOrderSyncAbstractService {

  protected final SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService;
  protected final SaleOrderLineBomService saleOrderLineBomService;
  protected final SaleOrderLineDetailsBomService saleOrderLineDetailsBomService;
  protected final SolBomCustomizationService solBomCustomizationService;
  protected final SolDetailsBomUpdateService solDetailsBomUpdateService;
  protected final SolBomUpdateService solBomUpdateService;
  protected final AppProductionService appProductionService;
  protected final SolDetailsProdProcessLineUpdateService solDetailsProdProcessLineUpdateService;
  protected final SolProdProcessCustomizationService solProdProcessCustomizationService;

  @Inject
  protected SaleOrderSyncAbstractService(
      SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService,
      SaleOrderLineBomService saleOrderLineBomService,
      SaleOrderLineDetailsBomService saleOrderLineDetailsBomService,
      SolBomCustomizationService solBomCustomizationService,
      SolDetailsBomUpdateService solDetailsBomUpdateService,
      SolBomUpdateService solBomUpdateService,
      AppProductionService appProductionService,
      SolDetailsProdProcessLineUpdateService solDetailsProdProcessLineUpdateService,
      SolProdProcessCustomizationService solProdProcessCustomizationService) {
    this.saleOrderLineBomLineMappingService = saleOrderLineBomLineMappingService;
    this.saleOrderLineBomService = saleOrderLineBomService;
    this.saleOrderLineDetailsBomService = saleOrderLineDetailsBomService;
    this.solBomCustomizationService = solBomCustomizationService;
    this.solDetailsBomUpdateService = solDetailsBomUpdateService;
    this.solBomUpdateService = solBomUpdateService;
    this.appProductionService = appProductionService;
    this.solDetailsProdProcessLineUpdateService = solDetailsProdProcessLineUpdateService;
    this.solProdProcessCustomizationService = solProdProcessCustomizationService;
  }

  /**
   * This method will synchronize the sale order lines and sub sale order lines.
   *
   * @param saleOrderLineList
   * @throws AxelorException
   */
  public void syncSaleOrderLineList(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList)
      throws AxelorException {
    AppProduction appProduction = appProductionService.getAppProduction();

    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return;
    }

    if (!appProduction.getAllowPersonalizedBOM()
        || appProduction.getIsBomLineGenerationInSODisabled()) {
      return;
    }

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      syncSaleOrderLine(saleOrder, saleOrderLine);
    }
  }

  public void syncSaleOrderLine(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Objects.requireNonNull(saleOrderLine);
    // No personalized BOM = no synchronization
    if (!appProductionService.getAppProduction().getAllowPersonalizedBOM()
        || !saleOrderLine.getIsToProduce()) {
      return;
    }

    // First we sync sub lines, because if a change occurs is one of them
    // We take it into account when sync the current sale order line
    if (saleOrderLine.getSubSaleOrderLineList() != null) {
      for (SaleOrderLine subSaleOrderLine : saleOrderLine.getSubSaleOrderLineList()) {
        syncSaleOrderLine(saleOrder, subSaleOrderLine);
      }
    }

    // if bom lines list is same size as sub line list (checking if more line or less)
    // and if each lines are sync
    updateLines(saleOrderLine);
    updateProdProcess(saleOrder, saleOrderLine);
  }

  protected void updateLines(SaleOrderLine saleOrderLine) throws AxelorException {
    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        getSaleOrderListDetailsList(saleOrderLine);
    var isUpdated = solBomUpdateService.isUpdated(saleOrderLine);
    var isSolDetailsUpdated =
        solDetailsBomUpdateService.isSolDetailsUpdated(saleOrderLine, saleOrderLineDetailsList);

    if (isUpdated && isSolDetailsUpdated) {
      return;
    }

    if (!isUpdated || !isSolDetailsUpdated) {
      if (!saleOrderLine.getBillOfMaterial().getPersonalized()) {
        solBomCustomizationService.customizeBomOf(saleOrderLine, saleOrderLineDetailsList);
      } else {
        if (!isUpdated) {
          solBomUpdateService.updateSolWithBillOfMaterial(saleOrderLine);
        }
        if (!isSolDetailsUpdated) {
          solDetailsBomUpdateService.updateSolDetailslWithBillOfMaterial(
              saleOrderLine, saleOrderLineDetailsList);
        }
      }
    }
  }

  protected void updateProdProcess(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        getSaleOrderListDetailsList(saleOrderLine);
    var isProdProcessLinesUpdated =
        solDetailsProdProcessLineUpdateService.isSolDetailsUpdated(
            saleOrderLine, saleOrderLineDetailsList);
    if (isProdProcessLinesUpdated) {
      return;
    }

    if (saleOrderLine.getProdProcess().getIsPersonalized()) {
      solProdProcessCustomizationService.updateProdProcessLines(
          saleOrder, saleOrderLine, saleOrderLineDetailsList);
    } else {
      solProdProcessCustomizationService.createCustomizedProdProcess(
          saleOrder, saleOrderLine, saleOrderLineDetailsList);
    }
  }

  protected abstract List<SaleOrderLineDetails> getSaleOrderListDetailsList(
      SaleOrderLine saleOrderLine);
}
