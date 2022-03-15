package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;

public interface SaleOrderAnalyticService {

  void checkSaleOrderAnalyticDistributionTemplate(SaleOrder saleOrder) throws AxelorException;
}
