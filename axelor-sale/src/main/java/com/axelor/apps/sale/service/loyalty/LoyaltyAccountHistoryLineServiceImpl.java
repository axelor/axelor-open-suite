package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.LoyaltyAccountHistoryLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class LoyaltyAccountHistoryLineServiceImpl implements LoyaltyAccountHistoryLineService {

  protected final AppBaseService appBaseService;
  protected final SaleOrderRepository saleOrderRepository;

  @Inject
  public LoyaltyAccountHistoryLineServiceImpl(
      AppBaseService appBaseService, SaleOrderRepository saleOrderRepository) {
    this.appBaseService = appBaseService;
    this.saleOrderRepository = saleOrderRepository;
  }

  @Override
  public LoyaltyAccountHistoryLine createHistoryLine(BigDecimal points, SaleOrder saleOrder) {
    LoyaltyAccountHistoryLine historyLine = new LoyaltyAccountHistoryLine();
    historyLine.setPointsBalance(points);
    historyLine.setSaleOrder(saleOrderRepository.find(saleOrder.getId()));
    return historyLine;
  }

  @Override
  public void spendAllPoints(LoyaltyAccountHistoryLine historyLine) {
    spendPoints(historyLine, historyLine.getRemainingPoints());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public BigDecimal spendPoints(LoyaltyAccountHistoryLine historyLine, BigDecimal points) {
    BigDecimal pointsLeft = historyLine.getRemainingPoints().subtract(points);
    historyLine.setRemainingPoints(pointsLeft);
    historyLine.setUseDateTime(appBaseService.getTodayDateTime().toLocalDateTime());
    return pointsLeft;
  }
}
