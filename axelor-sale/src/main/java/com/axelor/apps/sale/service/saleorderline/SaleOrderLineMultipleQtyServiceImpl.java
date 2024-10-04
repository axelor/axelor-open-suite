package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductMultipleQty;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.studio.db.AppSale;
import com.axelor.studio.db.repo.AppSaleRepository;
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

    if (appSale.getListDisplayTypeSelect() == AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_EDITABLE
        && !productMultipleQtyService.isMultipleQty(qty, productMultipleQtyList)) {
      return productMultipleQtyService.getMultipleQuantityErrorMessage(productMultipleQtyList);
    }
    return "";
  }
}
