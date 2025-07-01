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
import com.axelor.apps.production.db.repo.ProdProcessLineRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SolProdProcessCustomizationServiceImpl implements SolProdProcessCustomizationService {

  protected final ProdProcessService prodProcessService;
  protected final ProdProcessRepository prodProcessRepository;
  protected final ProdProcessLineRepository prodProcessLineRepository;
  protected final SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService;
  protected final SolDetailsProdProcessComputeQtyService solDetailsProdProcessComputeQtyService;

  @Inject
  public SolProdProcessCustomizationServiceImpl(
      ProdProcessService prodProcessService,
      ProdProcessRepository prodProcessRepository,
      ProdProcessLineRepository prodProcessLineRepository,
      SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService,
      SolDetailsProdProcessComputeQtyService solDetailsProdProcessComputeQtyService) {
    this.prodProcessService = prodProcessService;
    this.prodProcessRepository = prodProcessRepository;
    this.prodProcessLineRepository = prodProcessLineRepository;
    this.saleOrderLineDetailsPriceService = saleOrderLineDetailsPriceService;
    this.solDetailsProdProcessComputeQtyService = solDetailsProdProcessComputeQtyService;
  }

  @Transactional(rollbackOn = {Exception.class})
  public ProdProcess createCustomizedProdProcess(
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      List<SaleOrderLineDetails> saleOrderLineDetailsList)
      throws AxelorException {
    ProdProcess prodProcess = saleOrderLine.getProdProcess();

    if (prodProcess == null) {
      return null;
    }
    ProdProcess personalizedProdProcess =
        prodProcessService.createCustomizedProdProcess(prodProcess, false);
    createProdProcessLine(
        saleOrder, saleOrderLine, saleOrderLineDetailsList, personalizedProdProcess);
    saleOrderLine.setProdProcess(personalizedProdProcess);
    return personalizedProdProcess;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void updateProdProcessLines(
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      List<SaleOrderLineDetails> saleOrderLineDetailsList)
      throws AxelorException {
    if (CollectionUtils.isNotEmpty(saleOrderLineDetailsList)) {
      for (SaleOrderLineDetails saleOrderLineDetails :
          saleOrderLineDetailsList.stream()
              .filter(line -> line.getTypeSelect() == SaleOrderLineDetailsRepository.TYPE_OPERATION)
              .collect(Collectors.toList())) {
        if (saleOrderLineDetails.getProdProcessLine() != null) {
          updateProdProcessLineFromSolDetails(
              saleOrderLineDetails, saleOrderLineDetails.getProdProcessLine());
          solDetailsProdProcessComputeQtyService.setQty(
              saleOrderLine, saleOrderLineDetails.getProdProcessLine(), saleOrderLineDetails);
          saleOrderLineDetailsPriceService.computePrices(
              saleOrderLineDetails, saleOrder, saleOrderLine);
        }
      }
    }
  }

  protected void createProdProcessLine(
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      List<SaleOrderLineDetails> saleOrderLineDetailsList,
      ProdProcess personalizedProdProcess)
      throws AxelorException {
    if (CollectionUtils.isNotEmpty(saleOrderLineDetailsList)) {
      for (SaleOrderLineDetails saleOrderLineDetails :
          saleOrderLineDetailsList.stream()
              .filter(line -> line.getTypeSelect() == SaleOrderLineDetailsRepository.TYPE_OPERATION)
              .collect(Collectors.toList())) {
        ProdProcessLine newProdProcessLine =
            createProdProcessLineFromSolDetails(saleOrderLineDetails, personalizedProdProcess);
        saleOrderLineDetails.setProdProcessLine(newProdProcessLine);
        solDetailsProdProcessComputeQtyService.setQty(
            saleOrderLine, saleOrderLineDetails.getProdProcessLine(), saleOrderLineDetails);
        saleOrderLineDetailsPriceService.computePrices(
            saleOrderLineDetails, saleOrder, saleOrderLine);
      }
    }
  }

  @Transactional
  protected ProdProcessLine createProdProcessLineFromSolDetails(
      SaleOrderLineDetails saleOrderLineDetails, ProdProcess personalizedProdProcess) {

    ProdProcessLine prodProcessLineToCopy =
        prodProcessLineRepository.copy(saleOrderLineDetails.getProdProcessLine(), true);

    updateProdProcessLineFromSolDetails(saleOrderLineDetails, prodProcessLineToCopy);

    if (personalizedProdProcess != null) {
      prodProcessLineToCopy.setProdProcess(personalizedProdProcess);
    }

    return prodProcessLineRepository.save(prodProcessLineToCopy);
  }

  protected void updateProdProcessLineFromSolDetails(
      SaleOrderLineDetails saleOrderLineDetails, ProdProcessLine prodProcessLineToCopy) {
    prodProcessLineToCopy.setMinCapacityPerCycle(saleOrderLineDetails.getMinCapacityPerCycle());
    prodProcessLineToCopy.setMaxCapacityPerCycle(saleOrderLineDetails.getMaxCapacityPerCycle());
    prodProcessLineToCopy.setDurationPerCycle(saleOrderLineDetails.getDurationPerCycle());
    prodProcessLineToCopy.setSetupDuration(saleOrderLineDetails.getSetupDuration());
    prodProcessLineToCopy.setStartingDuration(saleOrderLineDetails.getStartingDuration());
    prodProcessLineToCopy.setEndingDuration(saleOrderLineDetails.getEndingDuration());
    prodProcessLineToCopy.setHumanDuration(saleOrderLineDetails.getHumanDuration());
    prodProcessLineToCopy.setCostTypeSelect(saleOrderLineDetails.getCostTypeSelect());
    prodProcessLineToCopy.setCostAmount(saleOrderLineDetails.getCostAmount());
    prodProcessLineToCopy.setHrCostTypeSelect(saleOrderLineDetails.getHrCostTypeSelect());
    prodProcessLineToCopy.setHrCostAmount(saleOrderLineDetails.getHrCostAmount());
    prodProcessLineRepository.save(prodProcessLineToCopy);
  }
}
