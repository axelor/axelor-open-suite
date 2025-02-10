package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.MarginComputeService;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineDetailsPriceServiceImpl implements SaleOrderLineDetailsPriceService {

  protected final MarginComputeService marginComputeService;
  protected final ProductCompanyService productCompanyService;
  protected final AppSaleService appSaleService;

  @Inject
  public SaleOrderLineDetailsPriceServiceImpl(
      MarginComputeService marginComputeService,
      ProductCompanyService productCompanyService,
      AppSaleService appSaleService) {
    this.marginComputeService = marginComputeService;
    this.productCompanyService = productCompanyService;
    this.appSaleService = appSaleService;
  }

  @Override
  public Map<String, Object> computePrices(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> lineMap = new HashMap<>();

    BigDecimal qty = saleOrderLineDetails.getQty();
    BigDecimal price = saleOrderLineDetails.getPrice();

    computeTotalPrice(saleOrderLineDetails, price, qty);
    computeTotalCostPrice(saleOrderLineDetails, saleOrder, qty);

    lineMap.putAll(
        marginComputeService.getComputedMarginInfo(
            saleOrder, saleOrderLineDetails, saleOrderLineDetails.getTotalPrice()));
    lineMap.put("totalCostPrice", saleOrderLineDetails.getTotalPrice());
    lineMap.put("subTotalCostPrice", saleOrderLineDetails.getSubTotalCostPrice());
    return lineMap;
  }

  protected void computeTotalPrice(
      SaleOrderLineDetails saleOrderLineDetails, BigDecimal price, BigDecimal qty) {
    BigDecimal totalPrice =
        price
            .multiply(qty)
            .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    saleOrderLineDetails.setTotalPrice(totalPrice);
  }

  protected void computeTotalCostPrice(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrder saleOrder, BigDecimal qty)
      throws AxelorException {
    Company company = saleOrder.getCompany();
    Product product = saleOrderLineDetails.getProduct();
    if (product != null && company != null) {
      BigDecimal costPrice = (BigDecimal) productCompanyService.get(product, "costPrice", company);
      BigDecimal totalCostPrice =
          costPrice
              .multiply(qty)
              .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
      saleOrderLineDetails.setSubTotalCostPrice(totalCostPrice);
    }
  }
}
