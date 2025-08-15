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
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SaleOrderLineDetailsProdProcessServiceImpl
    implements SaleOrderLineDetailsProdProcessService {

  protected final SolDetailsProdProcessLineMappingService solDetailsProdProcessLineMappingService;
  protected final AppProductionService appProductionService;

  @Inject
  public SaleOrderLineDetailsProdProcessServiceImpl(
      SolDetailsProdProcessLineMappingService solDetailsProdProcessLineMappingService,
      AppProductionService appProductionService) {
    this.solDetailsProdProcessLineMappingService = solDetailsProdProcessLineMappingService;
    this.appProductionService = appProductionService;
  }

  @Override
  public void addSaleOrderLineDetailsFromProdProcess(
      ProdProcess prodProcess, SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    createSaleOrderLineDetailsFromProdProcess(prodProcess, saleOrder, saleOrderLine).stream()
        .filter(Objects::nonNull)
        .forEach(saleOrderLine::addSaleOrderLineDetailsListItem);
  }

  @Override
  public List<SaleOrderLineDetails> createSaleOrderLineDetailsFromProdProcess(
      ProdProcess prodProcess, SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    if (appProductionService.getAppProduction().getIsProdProcessLineGenerationInSODisabled()) {
      return List.of();
    }
    Objects.requireNonNull(prodProcess);

    var saleOrderLinesDetailsList = new ArrayList<SaleOrderLineDetails>();

    for (ProdProcessLine prodProcessLine : prodProcess.getProdProcessLineList()) {
      var saleOrderLineDetails =
          solDetailsProdProcessLineMappingService.mapToSaleOrderLineDetails(
              saleOrder, saleOrderLine, prodProcessLine);
      if (saleOrderLineDetails != null) {
        saleOrderLinesDetailsList.add(saleOrderLineDetails);
      }
    }

    return saleOrderLinesDetailsList;
  }

  @Override
  public List<SaleOrderLineDetails> getUpdatedSaleOrderLineDetailsFromProdProcess(
      ProdProcess prodProcess, SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    List<SaleOrderLineDetails> componentSolDetailsLineList =
        saleOrderLine.getSaleOrderLineDetailsList().stream()
            .filter(line -> line.getTypeSelect() == SaleOrderLineDetailsRepository.TYPE_COMPONENT)
            .collect(Collectors.toList());
    List<SaleOrderLineDetails> updatedSolDetailsLineList =
        createSaleOrderLineDetailsFromProdProcess(prodProcess, saleOrder, saleOrderLine);
    updatedSolDetailsLineList.addAll(componentSolDetailsLineList);
    return updatedSolDetailsLineList;
  }
}
