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
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.studio.db.AppProduction;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SolBomCustomizationServiceImpl implements SolBomCustomizationService {

  protected final BillOfMaterialService billOfMaterialService;
  protected final BillOfMaterialRepository billOfMaterialRepository;
  protected final BomLineCreationService bomLineCreationService;
  protected final AppProductionService appProductionService;
  protected final SolDetailsBomUpdateService solDetailsBomUpdateService;
  protected final SolBomUpdateService solBomUpdateService;

  @Inject
  public SolBomCustomizationServiceImpl(
      BillOfMaterialService billOfMaterialService,
      BillOfMaterialRepository billOfMaterialRepository,
      BomLineCreationService bomLineCreationService,
      AppProductionService appProductionService,
      SolDetailsBomUpdateService solDetailsBomUpdateService,
      SolBomUpdateService solBomUpdateService) {
    this.billOfMaterialService = billOfMaterialService;
    this.billOfMaterialRepository = billOfMaterialRepository;
    this.bomLineCreationService = bomLineCreationService;
    this.appProductionService = appProductionService;
    this.solDetailsBomUpdateService = solDetailsBomUpdateService;
    this.solBomUpdateService = solBomUpdateService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void customizeBomOf(
      SaleOrderLine saleOrderLine, List<SaleOrderLineDetails> saleOrderLineDetailsList)
      throws AxelorException {
    customizeBomOf(saleOrderLine, saleOrderLineDetailsList, 0);
  }

  protected BillOfMaterial customizeBomOf(
      SaleOrderLine saleOrderLine, List<SaleOrderLineDetails> saleOrderLineDetailsList, int depth)
      throws AxelorException {
    var billOfMaterial = saleOrderLine.getBillOfMaterial();
    if (billOfMaterial == null) {
      return null;
    }
    BillOfMaterial personalizedBOM =
        billOfMaterialService.getCustomizedBom(billOfMaterial, depth, false);

    saleOrderLine.setBillOfMaterial(personalizedBOM);

    createSemiFinishedProductBomLines(
        saleOrderLine, saleOrderLineDetailsList, depth, personalizedBOM);
    createComponentBomLines(saleOrderLineDetailsList, personalizedBOM);

    return billOfMaterialRepository.save(personalizedBOM);
  }

  protected void createSemiFinishedProductBomLines(
      SaleOrderLine saleOrderLine,
      List<SaleOrderLineDetails> saleOrderLineDetailsList,
      int depth,
      BillOfMaterial personalizedBOM)
      throws AxelorException {
    if (saleOrderLine.getSubSaleOrderLineList() != null) {
      List<SaleOrderLine> subSaleOrderLineList =
          saleOrderLine.getSubSaleOrderLineList().stream()
              .filter(line -> line.getProduct().getProductSubTypeSelect() != null)
              .collect(Collectors.toList());
      for (SaleOrderLine subSaleOrderLine : subSaleOrderLineList) {
        if (subSaleOrderLine
            .getProduct()
            .getProductSubTypeSelect()
            .equals(ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT)) {
          var bomLine = bomLineCreationService.createBomLineFromSol(subSaleOrderLine);
          // Relink billOfMaterialLine
          subSaleOrderLine.setBillOfMaterialLine(bomLine);
          personalizedBOM.addBillOfMaterialLineListItem(bomLine);
        }
      }
    }
  }

  protected void createComponentBomLines(
      List<SaleOrderLineDetails> saleOrderLineDetailsList, BillOfMaterial personalizedBOM) {
    if (CollectionUtils.isNotEmpty(saleOrderLineDetailsList)) {
      for (SaleOrderLineDetails saleOrderLineDetails : saleOrderLineDetailsList) {
        if (saleOrderLineDetails.getTypeSelect() == SaleOrderLineDetailsRepository.TYPE_COMPONENT) {
          var bomLine = bomLineCreationService.createBomLineFromSolDetails(saleOrderLineDetails);
          saleOrderLineDetails.setBillOfMaterialLine(bomLine);
          personalizedBOM.addBillOfMaterialLineListItem(bomLine);
        }
      }
    }
  }

  @Override
  public void customSaleOrderLineList(List<SaleOrderLine> saleOrderLineList)
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
      customSaleOrderLine(saleOrderLine);
    }
  }

  protected void customSaleOrderLine(SaleOrderLine saleOrderLine) throws AxelorException {
    Objects.requireNonNull(saleOrderLine);
    if (!appProductionService.getAppProduction().getAllowPersonalizedBOM()
        || !saleOrderLine.getIsToProduce()) {
      return;
    }

    if (saleOrderLine.getSubSaleOrderLineList() != null) {
      for (SaleOrderLine subSaleOrderLine : saleOrderLine.getSubSaleOrderLineList()) {
        customSaleOrderLine(subSaleOrderLine);
      }
    }

    customizeBomOf(saleOrderLine);
  }

  protected void customizeBomOf(SaleOrderLine saleOrderLine) throws AxelorException {
    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        saleOrderLine.getSaleOrderLineDetailsList();
    var isUpdated = solBomUpdateService.isUpdated(saleOrderLine);
    var isSolDetailsUpdated =
        solDetailsBomUpdateService.isSolDetailsUpdated(saleOrderLine, saleOrderLineDetailsList);

    if (isUpdated && isSolDetailsUpdated) {
      return;
    }

    if (!isSolDetailsUpdated || !isUpdated) {
      customizeBomOf(saleOrderLine, saleOrderLineDetailsList);
    }
  }
}
