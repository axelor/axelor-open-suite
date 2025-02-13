package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.subline.SubSaleOrderLineComputeServiceImpl;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SubSaleOrderLineComputeServiceProductionImpl
    extends SubSaleOrderLineComputeServiceImpl {
  @Inject
  public SubSaleOrderLineComputeServiceProductionImpl(
      SaleOrderLineComputeService saleOrderLineComputeService,
      AppSaleService appSaleService,
      CurrencyScaleService currencyScaleService) {
    super(saleOrderLineComputeService, appSaleService, currencyScaleService);
  }

  @Override
  public void computeSumSubLineList(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        saleOrderLine.getSaleOrderLineDetailsList();
    BigDecimal totalPrice = BigDecimal.ZERO;
    BigDecimal totalCostPrice = BigDecimal.ZERO;
    BigDecimal subDetailsTotalCostPrice;
    BigDecimal subDetailsTotal;
    if (appSaleService.getAppSale().getIsSOLPriceTotalOfSubLines()
        && (CollectionUtils.isNotEmpty(subSaleOrderLineList))) {
      for (SaleOrderLine subSaleOrderLine : subSaleOrderLineList) {
        computeSumSubLineList(subSaleOrderLine, saleOrder);
      }
      if (CollectionUtils.isNotEmpty(saleOrderLineDetailsList)) {
        subDetailsTotal =
            saleOrderLineDetailsList.stream()
                .map(SaleOrderLineDetails::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        subDetailsTotalCostPrice =
            saleOrderLineDetailsList.stream()
                .map(SaleOrderLineDetails::getSubTotalCostPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalPrice = totalPrice.add(subDetailsTotal);
        totalCostPrice = totalCostPrice.add(subDetailsTotalCostPrice);
        saleOrderLine.setPrice(totalPrice);
        saleOrderLine.setSubTotalCostPrice(
            currencyScaleService.getCompanyScaledValue(saleOrder, totalCostPrice));
      }
      totalPrice =
          totalPrice.add(
              subSaleOrderLineList.stream()
                  .map(SaleOrderLine::getExTaxTotal)
                  .reduce(BigDecimal.ZERO, BigDecimal::add));
      totalCostPrice =
          totalCostPrice.add(
              subSaleOrderLineList.stream()
                  .map(SaleOrderLine::getSubTotalCostPrice)
                  .reduce(BigDecimal.ZERO, BigDecimal::add));
      saleOrderLine.setPrice(totalPrice);
      saleOrderLine.setSubTotalCostPrice(totalCostPrice);
      saleOrderLineComputeService.computeValues(saleOrder, saleOrderLine);
    } else {
      saleOrderLineComputeService.computeValues(saleOrder, saleOrderLine);
    }
  }
}
