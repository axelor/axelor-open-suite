package com.axelor.apps.production.service;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SaleOrderLineBomServiceImpl implements SaleOrderLineBomService {

  protected final SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService;

  @Inject
  public SaleOrderLineBomServiceImpl(
      SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService) {
    this.saleOrderLineBomLineMappingService = saleOrderLineBomLineMappingService;
  }

  @Override
  public List<SaleOrderLine> createSaleOrderLinesFromBom(BillOfMaterial billOfMaterial) {
    Objects.requireNonNull(billOfMaterial);

    return billOfMaterial.getBillOfMaterialLineList().stream()
        .map(saleOrderLineBomLineMappingService::mapToSaleOrderLine)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
