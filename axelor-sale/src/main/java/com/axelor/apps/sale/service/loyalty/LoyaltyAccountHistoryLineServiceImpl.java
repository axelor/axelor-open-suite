package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.sale.db.LoyaltyAccountHistoryLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoyaltyAccountHistoryLineServiceImpl implements LoyaltyAccountHistoryLineService {

  protected final SaleOrderRepository saleOrderRepository;

  @Inject
  public LoyaltyAccountHistoryLineServiceImpl(SaleOrderRepository saleOrderRepository) {
    this.saleOrderRepository = saleOrderRepository;
  }

  @Override
  public LoyaltyAccountHistoryLine createHistoryLine(
      BigDecimal points, LocalDateTime acquisitionDateTime, SaleOrder saleOrder) {
    LoyaltyAccountHistoryLine historyLine = new LoyaltyAccountHistoryLine();
    historyLine.setPointsBalance(points);
    historyLine.setRemainingPoints(points);
    historyLine.setAcquisitionDateTime(acquisitionDateTime);
    historyLine.setSaleOrder(saleOrderRepository.find(saleOrder.getId()));
    return historyLine;
  }
}
