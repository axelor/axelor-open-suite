package com.axelor.apps.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.PriceStudy;
import com.axelor.apps.sale.db.SaleOrder;

public interface PriceStudyService {
  void recalculatePrices(SaleOrder saleOrder) throws AxelorException;

  void onPriceChange(PriceStudy priceStudy);

  void onGeneralExpensesChange(PriceStudy priceStudy);

  void onMargeChange(PriceStudy priceStudy);
}
