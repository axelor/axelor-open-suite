package com.axelor.apps.production.service;

import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.Objects;

public class SaleOrderLineBomLineMappingServiceImpl implements SaleOrderLineBomLineMappingService {

  protected final SaleOrderLineBomService saleOrderLineBomService;

  @Inject
  public SaleOrderLineBomLineMappingServiceImpl(SaleOrderLineBomService saleOrderLineBomService) {
    this.saleOrderLineBomService = saleOrderLineBomService;
  }

  @Override
  public SaleOrderLine mapToSaleOrderLine(BillOfMaterialLine billOfMaterialLine) {
    Objects.requireNonNull(billOfMaterialLine);

    if (billOfMaterialLine.getProduct().getProductSubTypeSelect()
        == ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT) {
      SaleOrderLine saleOrderLine = new SaleOrderLine();
      saleOrderLine.setProduct(billOfMaterialLine.getProduct());
      saleOrderLine.setProductName(billOfMaterialLine.getProduct().getName());
      saleOrderLine.setQty(billOfMaterialLine.getQty());
      saleOrderLine.setUnit(billOfMaterialLine.getUnit());

      if (billOfMaterialLine.getBillOfMaterial() != null) {
        saleOrderLineBomService
            .createSaleOrderLinesFromBom(billOfMaterialLine.getBillOfMaterial())
            .forEach(saleOrderLine::addSubSaleOrderLineListItem);
      }

      return saleOrderLine;
    }
    return null;
  }
}
