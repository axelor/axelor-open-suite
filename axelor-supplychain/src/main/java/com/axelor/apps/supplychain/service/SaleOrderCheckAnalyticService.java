package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;

public interface SaleOrderCheckAnalyticService {

  /**
   * Checks every sale order line for analytic distribution. An exception will be thrown with the
   * list of lines missing analytic distribution information.
   *
   * @param saleOrder a non null sale order
   * @throws AxelorException if one or more lines are missing an analytic distribution
   */
  void checkSaleOrderLinesAnalyticDistribution(SaleOrder saleOrder) throws AxelorException;
}
