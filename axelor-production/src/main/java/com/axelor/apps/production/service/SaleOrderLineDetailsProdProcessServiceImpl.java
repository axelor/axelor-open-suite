package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

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
      ProdProcess prodProcess, SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Objects.requireNonNull(prodProcess);
    List<SaleOrderLineDetails> originSaleOrderLineDetailsList =
        saleOrderLine.getSaleOrderLineDetailsList();
    List<SaleOrderLineDetails> filteredSaleOrderLineDetails = new ArrayList<>();

    if (CollectionUtils.isNotEmpty(originSaleOrderLineDetailsList)) {
      filteredSaleOrderLineDetails.addAll(
          originSaleOrderLineDetailsList.stream()
              .filter(line -> line.getTypeSelect() != SaleOrderLineDetailsRepository.TYPE_OPERATION)
              .collect(Collectors.toList()));
    }

    var saleOrderLinesDetailsList = new ArrayList<SaleOrderLineDetails>();

    for (ProdProcessLine prodProcessLine : prodProcess.getProdProcessLineList()) {
      var saleOrderLineDetails =
          solDetailsProdProcessLineMappingService.mapToSaleOrderLineDetails(
              saleOrder, saleOrderLine, prodProcessLine);
      if (saleOrderLineDetails != null) {
        saleOrderLinesDetailsList.add(saleOrderLineDetails);
      }
    }
    filteredSaleOrderLineDetails.addAll(saleOrderLinesDetailsList);
    return filteredSaleOrderLineDetails;
  }
}
