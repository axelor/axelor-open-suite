package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCostPriceComputeServiceImpl;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineCostPriceComputeProductionServiceImpl
    extends SaleOrderLineCostPriceComputeServiceImpl {

  @Inject
  public SaleOrderLineCostPriceComputeProductionServiceImpl(
      AppSaleService appSaleService,
      ProductCompanyService productCompanyService,
      CurrencyScaleService currencyScaleService) {
    super(appSaleService, productCompanyService, currencyScaleService);
  }

  @Override
  public Map<String, Object> computeSubTotalCostPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Product product) throws AxelorException {
    Map<String, Object> map = new HashMap<>();
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        saleOrderLine.getSaleOrderLineDetailsList();
    if (appSaleService.getAppSale().getListDisplayTypeSelect()
            != AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_MULTI
        || (CollectionUtils.isEmpty(subSaleOrderLineList)
            && CollectionUtils.isEmpty(saleOrderLineDetailsList))) {
      return super.computeSubTotalCostPrice(saleOrder, saleOrderLine, product);
    }
    BigDecimal costPriceTotal = BigDecimal.ZERO;
    if (CollectionUtils.isNotEmpty(subSaleOrderLineList)) {
      costPriceTotal =
          costPriceTotal.add(
              subSaleOrderLineList.stream()
                  .map(SaleOrderLine::getSubTotalCostPrice)
                  .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
    if (CollectionUtils.isNotEmpty(saleOrderLineDetailsList)) {
      costPriceTotal =
          costPriceTotal.add(
              saleOrderLineDetailsList.stream()
                  .map(SaleOrderLineDetails::getSubTotalCostPrice)
                  .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    saleOrderLine.setSubTotalCostPrice(
        currencyScaleService.getCompanyScaledValue(
            saleOrder, costPriceTotal.multiply(saleOrderLine.getQty())));
    map.put("subTotalCostPrice", saleOrderLine.getSubTotalCostPrice());
    return map;
  }
}
