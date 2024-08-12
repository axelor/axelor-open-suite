package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.sale.db.LoyaltyAccountHistoryLine;
import com.axelor.apps.sale.db.SaleOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface LoyaltyAccountHistoryLineService {
  LoyaltyAccountHistoryLine createHistoryLine(
      BigDecimal points, LocalDateTime acquisitionDateTime, SaleOrder saleOrder);
}
