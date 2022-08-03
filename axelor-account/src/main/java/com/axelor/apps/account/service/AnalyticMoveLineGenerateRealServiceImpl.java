package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.google.inject.Inject;

public class AnalyticMoveLineGenerateRealServiceImpl
    implements AnalyticMoveLineGenerateRealService {

  protected AnalyticMoveLineRepository analyticMoveLineRepository;
  protected AnalyticMoveLineService analyticMoveLineService;

  @Inject
  public AnalyticMoveLineGenerateRealServiceImpl(
      AnalyticMoveLineRepository analyticMoveLineRepository,
      AnalyticMoveLineService analyticMoveLineService) {
    this.analyticMoveLineRepository = analyticMoveLineRepository;
    this.analyticMoveLineService = analyticMoveLineService;
  }

  @Override
  public AnalyticMoveLine createFromForecast(
      AnalyticMoveLine forecastAnalyticMoveLine, MoveLine moveLine) {
    AnalyticMoveLine analyticMoveLine =
        analyticMoveLineRepository.copy(forecastAnalyticMoveLine, false);
    analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING);
    analyticMoveLine.setInvoiceLine(null);
    analyticMoveLine.setAccount(moveLine.getAccount());
    analyticMoveLine.setAccountType(moveLine.getAccount().getAccountType());
    analyticMoveLineService.updateAnalyticMoveLine(
        analyticMoveLine, moveLine.getDebit().add(moveLine.getCredit()), moveLine.getDate());
    return analyticMoveLine;
  }
}
