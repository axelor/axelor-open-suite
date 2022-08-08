package com.axelor.apps.hr.service.analytic;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.google.inject.Inject;

public class AnalyticMoveLineGenerateRealServiceHrImpl
    extends AnalyticMoveLineGenerateRealServiceImpl {

  @Inject
  public AnalyticMoveLineGenerateRealServiceHrImpl(
      AnalyticMoveLineRepository analyticMoveLineRepository,
      AnalyticMoveLineService analyticMoveLineService) {
    super(analyticMoveLineRepository, analyticMoveLineService);
  }

  @Override
  public AnalyticMoveLine createFromForecast(
      AnalyticMoveLine forecastAnalyticMoveLine, MoveLine moveLine) {
    AnalyticMoveLine analyticMoveLine =
        super.createFromForecast(forecastAnalyticMoveLine, moveLine);
    analyticMoveLine.setExpenseLine(null);
    return analyticMoveLine;
  }
}
