package com.axelor.apps.production.service;

import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

public class BomLineCreationServiceImpl implements BomLineCreationService {

  protected final BillOfMaterialLineService billOfMaterialLineService;

  @Inject
  public BomLineCreationServiceImpl(BillOfMaterialLineService billOfMaterialLineService) {
    this.billOfMaterialLineService = billOfMaterialLineService;
  }

  @Override
  public BillOfMaterialLine createBomLineFromSol(SaleOrderLine subSaleOrderLine) {
    return billOfMaterialLineService.createBillOfMaterialLine(
        subSaleOrderLine.getProduct(),
        subSaleOrderLine.getBillOfMaterial(),
        subSaleOrderLine.getQty(),
        subSaleOrderLine.getUnit(),
        Optional.ofNullable(subSaleOrderLine.getSequence())
            .map(seq -> seq * 10)
            .or(
                () ->
                    Optional.ofNullable(subSaleOrderLine.getBillOfMaterialLine())
                        .map(BillOfMaterialLine::getPriority))
            .orElse(0),
        subSaleOrderLine.getProduct().getStockManaged(),
        Optional.ofNullable(subSaleOrderLine.getBillOfMaterialLine())
            .map(BillOfMaterialLine::getWasteRate)
            .orElse(BigDecimal.ZERO));
  }

  @Override
  public BillOfMaterialLine createBomLineFromSolDetails(SaleOrderLineDetails saleOrderLineDetails) {
    return billOfMaterialLineService.createBillOfMaterialLine(
        saleOrderLineDetails.getProduct(),
        null,
        saleOrderLineDetails.getQty(),
        saleOrderLineDetails.getUnit(),
        Optional.ofNullable(saleOrderLineDetails.getBillOfMaterialLine())
            .map(BillOfMaterialLine::getPriority)
            .orElse(0),
        saleOrderLineDetails.getProduct().getStockManaged(),
        BigDecimal.ZERO);
  }
}
