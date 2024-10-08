package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineDetailsServiceImpl implements SaleOrderLineDetailsService {
  protected ProductCompanyService productCompanyService;
  protected AppSaleService appSaleService;
  protected SaleOrderLineProductService saleOrderLineProductService;

  @Inject
  public SaleOrderLineDetailsServiceImpl(
      ProductCompanyService productCompanyService,
      AppSaleService appSaleService,
      SaleOrderLineProductService saleOrderLineProductService) {
    this.productCompanyService = productCompanyService;
    this.appSaleService = appSaleService;
    this.saleOrderLineProductService = saleOrderLineProductService;
  }

  @Override
  public Map<String, Object> productOnChange(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> lineMap = new HashMap<>();
    Company company = saleOrder.getCompany();
    Product product = saleOrderLineDetails.getProduct();
    if (product == null) {
      return lineMap;
    }
    BigDecimal price = (BigDecimal) productCompanyService.get(product, "salePrice", company);
    BigDecimal totalPrice =
        price
            .multiply(saleOrderLineDetails.getQty())
            .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);

    saleOrderLineDetails.setPrice(price);
    saleOrderLineDetails.setTotalPrice(totalPrice);
    saleOrderLineDetails.setTitle(product.getName());
    saleOrderLineDetails.setUnit(saleOrderLineProductService.getSaleUnit(product));

    lineMap.put("price", saleOrderLineDetails.getPrice());
    lineMap.put("totalPrice", saleOrderLineDetails.getTotalPrice());
    lineMap.put("title", saleOrderLineDetails.getTitle());
    lineMap.put("unit", saleOrderLineDetails.getUnit());
    return lineMap;
  }
}
