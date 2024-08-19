package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.sale.db.LoyaltyAccountHistoryLine;
import com.axelor.apps.sale.db.SaleOrder;
import java.math.BigDecimal;

public interface LoyaltyAccountHistoryLineService {
  LoyaltyAccountHistoryLine createHistoryLine(BigDecimal points, SaleOrder saleOrder);
}
