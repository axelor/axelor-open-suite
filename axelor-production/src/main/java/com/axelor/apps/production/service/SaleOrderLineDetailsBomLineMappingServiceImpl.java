package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;
import java.util.Objects;

public class SaleOrderLineDetailsBomLineMappingServiceImpl
    implements SaleOrderLineDetailsBomLineMappingService {

  protected final SaleOrderLineDetailsService saleOrderLineDetailsService;

  @Inject
  public SaleOrderLineDetailsBomLineMappingServiceImpl(
      SaleOrderLineDetailsService saleOrderLineDetailsService) {
    this.saleOrderLineDetailsService = saleOrderLineDetailsService;
  }

  @Override
  public SaleOrderLineDetails mapToSaleOrderLineDetails(
      BillOfMaterialLine billOfMaterialLine, SaleOrder saleOrder) throws AxelorException {
    Objects.requireNonNull(billOfMaterialLine);

    if (billOfMaterialLine.getProduct().getProductSubTypeSelect()
        == ProductRepository.PRODUCT_SUB_TYPE_COMPONENT) {
      SaleOrderLineDetails saleOrderLineDetails = new SaleOrderLineDetails();
      saleOrderLineDetails.setTypeSelect(SaleOrderLineDetailsRepository.TYPE_COMPONENT);
      saleOrderLineDetails.setProduct(billOfMaterialLine.getProduct());
      saleOrderLineDetails.setQty(billOfMaterialLine.getQty());
      saleOrderLineDetailsService.productOnChange(saleOrderLineDetails, saleOrder);

      return saleOrderLineDetails;
    }
    return null;
  }
}
