package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductMultipleQty;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.studio.db.AppSale;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class SaleOrderLineMultipleQtyServiceImpl implements SaleOrderLineMultipleQtyService {

  protected ProductMultipleQtyService productMultipleQtyService;
  protected AppSaleService appSaleService;

  @Inject
  public SaleOrderLineMultipleQtyServiceImpl(
      ProductMultipleQtyService productMultipleQtyService, AppSaleService appSaleService) {
    this.productMultipleQtyService = productMultipleQtyService;
    this.appSaleService = appSaleService;
  }

  @Override
  public String getMultipleQtyErrorMessage(SaleOrderLine saleOrderLine) {

    AppSale appSale = appSaleService.getAppSale();
    Product product = saleOrderLine.getProduct();

    if (product == null || !appSale.getManageMultipleSaleQuantity()) {
      return "";
    }

    BigDecimal qty = saleOrderLine.getQty();
    List<ProductMultipleQty> productMultipleQtyList = product.getSaleProductMultipleQtyList();

    if (appSaleService.getAppSale().getIsEditableGridEnabled()
        && !productMultipleQtyService.checkMultipleQty(qty, productMultipleQtyList)) {
      return productMultipleQtyService.getMultipleQuantityErrorMessage(productMultipleQtyList);
    }
    return "";
  }
}
