package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.util.ArrayList;
import java.util.List;

public class SaleOrderCheckAnalyticServiceImpl implements SaleOrderCheckAnalyticService {

  @Override
  public void checkSaleOrderLinesAnalyticDistribution(SaleOrder saleOrder) throws AxelorException {

    List<String> productList = new ArrayList<>();
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      if (saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_NORMAL
          && saleOrderLine.getAnalyticDistributionTemplate() == null) {
        productList.add(saleOrderLine.getProductName());
      }
    }
    if (!productList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.SALE_ORDER_ANALYTIC_DISTRIBUTION_ERROR),
          productList);
    }
  }
}
