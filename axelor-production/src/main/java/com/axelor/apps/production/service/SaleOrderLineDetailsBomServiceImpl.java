package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SaleOrderLineDetailsBomServiceImpl implements SaleOrderLineDetailsBomService {

  protected final SaleOrderLineDetailsBomLineMappingService
      saleOrderLineDetailsBomLineMappingService;

  @Inject
  public SaleOrderLineDetailsBomServiceImpl(
      SaleOrderLineDetailsBomLineMappingService saleOrderLineDetailsBomLineMappingService) {
    this.saleOrderLineDetailsBomLineMappingService = saleOrderLineDetailsBomLineMappingService;
  }

  @Override
  public List<SaleOrderLineDetails> createSaleOrderLineDetailsFromBom(
      BillOfMaterial billOfMaterial, SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Objects.requireNonNull(billOfMaterial);

    List<SaleOrderLineDetails> filteredSaleOrderLineDetails =
        saleOrderLine.getSaleOrderLineDetailsList().stream()
            .filter(line -> line.getTypeSelect() != SaleOrderLineDetailsRepository.TYPE_COMPONENT)
            .collect(Collectors.toList());
    var saleOrderLinesDetailsList = new ArrayList<SaleOrderLineDetails>();

    for (BillOfMaterialLine billOfMaterialLine : billOfMaterial.getBillOfMaterialLineList()) {
      var saleOrderLineDetails =
          saleOrderLineDetailsBomLineMappingService.mapToSaleOrderLineDetails(
              billOfMaterialLine, saleOrder);
      if (saleOrderLineDetails != null) {
        saleOrderLinesDetailsList.add(saleOrderLineDetails);
      }
    }

    filteredSaleOrderLineDetails.addAll(saleOrderLinesDetailsList);
    return filteredSaleOrderLineDetails;
  }
}
