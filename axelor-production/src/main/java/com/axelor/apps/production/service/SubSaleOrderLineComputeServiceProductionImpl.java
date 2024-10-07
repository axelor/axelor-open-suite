package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
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
      SaleOrderLineComputeService saleOrderLineComputeService) {
    super(saleOrderLineComputeService);
  }

  @Override
  public BigDecimal computeSumSubLineList(
      List<SaleOrderLine> subSaleOrderLineList, SaleOrder saleOrder) throws AxelorException {
    for (SaleOrderLine subSaleOrderLine : subSaleOrderLineList) {
      List<SaleOrderLine> subSubSaleOrderLineList = subSaleOrderLine.getSubSaleOrderLineList();
      List<SaleOrderLineDetails> saleOrderLineDetailsList =
          subSaleOrderLine.getSaleOrderLineDetailsList();
      BigDecimal totalPrice = subSaleOrderLine.getPrice();
      BigDecimal subDetailsTotal = BigDecimal.ZERO;
      if (CollectionUtils.isNotEmpty(subSubSaleOrderLineList)) {
        totalPrice = BigDecimal.ZERO;
        totalPrice =
            totalPrice.add(
                computeSumSubLineList(subSubSaleOrderLineList, saleOrder).add(subDetailsTotal));
        if (CollectionUtils.isNotEmpty(saleOrderLineDetailsList)) {
          subDetailsTotal =
              saleOrderLineDetailsList.stream()
                  .map(SaleOrderLineDetails::getTotalPrice)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
          totalPrice = totalPrice.add(subDetailsTotal);
        }
      }

      subSaleOrderLine.setPrice(totalPrice);
      saleOrderLineComputeService.computeValues(saleOrder, subSaleOrderLine);
    }
    return subSaleOrderLineList.stream()
        .map(SaleOrderLine::getExTaxTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
