package com.axelor.apps.production.service;

import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SaleOrderLineDetailsProdProcessServiceImpl
    implements SaleOrderLineDetailsProdProcessService {

  protected final SolDetailsProdProcessLineMappingService solDetailsProdProcessLineMappingService;

  @Inject
  public SaleOrderLineDetailsProdProcessServiceImpl(
      SolDetailsProdProcessLineMappingService solDetailsProdProcessLineMappingService) {
    this.solDetailsProdProcessLineMappingService = solDetailsProdProcessLineMappingService;
  }

  @Override
  public List<SaleOrderLineDetails> createSaleOrderLineDetailsFromProdProcess(
      ProdProcess prodProcess, SaleOrderLine saleOrderLine) {
    Objects.requireNonNull(prodProcess);
    List<SaleOrderLineDetails> filteredSaleOrderLineDetails =
        saleOrderLine.getSaleOrderLineDetailsList().stream()
            .filter(line -> line.getTypeSelect() != SaleOrderLineDetailsRepository.TYPE_OPERATION)
            .collect(Collectors.toList());

    var saleOrderLinesDetailsList = new ArrayList<SaleOrderLineDetails>();

    for (ProdProcessLine prodProcessLine : prodProcess.getProdProcessLineList()) {
      var saleOrderLineDetails =
          solDetailsProdProcessLineMappingService.mapToSaleOrderLineDetails(prodProcessLine);
      if (saleOrderLineDetails != null) {
        saleOrderLinesDetailsList.add(saleOrderLineDetails);
      }
    }
    filteredSaleOrderLineDetails.addAll(saleOrderLinesDetailsList);
    return filteredSaleOrderLineDetails;
  }
}
