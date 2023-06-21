package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticLevel;
import com.axelor.apps.account.db.repo.AnalyticLevelRepository;
import com.google.inject.Inject;

public class AccountingReportAnalyticConfigLineServiceImpl
    implements AccountingReportAnalyticConfigLineService {
  protected AnalyticLevelRepository analyticLevelRepo;

  @Inject
  public AccountingReportAnalyticConfigLineServiceImpl(AnalyticLevelRepository analyticLevelRepo) {
    this.analyticLevelRepo = analyticLevelRepo;
  }

  @Override
  public boolean getIsNotValidRuleLevel(int ruleLevel) {
    int maxLevel =
        analyticLevelRepo.all().fetch().stream()
            .map(AnalyticLevel::getNbr)
            .max(Integer::compareTo)
            .orElse(1);

    return ruleLevel > maxLevel;
  }
}
