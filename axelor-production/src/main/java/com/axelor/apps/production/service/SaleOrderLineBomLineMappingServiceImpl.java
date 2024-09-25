package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineOnProductChangeService;
import com.google.inject.Inject;
import java.util.Objects;

public class SaleOrderLineBomLineMappingServiceImpl implements SaleOrderLineBomLineMappingService {

  protected final SaleOrderLineBomService saleOrderLineBomService;
  protected final SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService;

  @Inject
  public SaleOrderLineBomLineMappingServiceImpl(
      SaleOrderLineBomService saleOrderLineBomService,
      SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService) {
    this.saleOrderLineBomService = saleOrderLineBomService;
    this.saleOrderLineOnProductChangeService = saleOrderLineOnProductChangeService;
  }

  @Override
  public SaleOrderLine mapToSaleOrderLine(
      BillOfMaterialLine billOfMaterialLine, SaleOrder saleOrder) throws AxelorException {
    Objects.requireNonNull(billOfMaterialLine);

    if (billOfMaterialLine.getProduct().getProductSubTypeSelect()
        == ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT) {
      SaleOrderLine saleOrderLine = new SaleOrderLine();
      saleOrderLine.setProduct(billOfMaterialLine.getProduct());
      saleOrderLine.setQty(billOfMaterialLine.getQty());
      saleOrderLineOnProductChangeService.computeLineFromProduct(saleOrder, saleOrderLine);
      return saleOrderLine;
    }
    return null;
  }
}
