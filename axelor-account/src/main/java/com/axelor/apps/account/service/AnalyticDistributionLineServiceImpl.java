package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticJournal;
import java.math.BigDecimal;

public class AnalyticDistributionLineServiceImpl implements AnalyticDistributionLineService {

  @Override
  public AnalyticDistributionLine createAnalyticDistributionLine(
      AnalyticAxis analyticAxis,
      AnalyticAccount analyticAccount,
      AnalyticJournal analyticJournal,
      BigDecimal percentage) {
    AnalyticDistributionLine analyticDistributionLine = new AnalyticDistributionLine();
    analyticDistributionLine.setAnalyticAxis(analyticAxis);
    analyticDistributionLine.setAnalyticAccount(analyticAccount);
    analyticDistributionLine.setAnalyticJournal(analyticJournal);
    analyticDistributionLine.setPercentage(percentage);
    return analyticDistributionLine;
  }
}
