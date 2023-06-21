package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticLevel;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.google.inject.Inject;

public class AccountingReportAnalyticConfigLineServiceImpl
    implements AccountingReportAnalyticConfigLineService {
  protected AnalyticAccountRepository analyticAccountRepo;

  @Inject
  public AccountingReportAnalyticConfigLineServiceImpl(
      AnalyticAccountRepository analyticAccountRepo) {
    this.analyticAccountRepo = analyticAccountRepo;
  }

  @Override
  public boolean getIsNotValidRuleLevel(int ruleLevel) {
    int maxLevel =
        analyticAccountRepo.all().fetch().stream()
            .map(AnalyticAccount::getAnalyticLevel)
            .map(AnalyticLevel::getNbr)
            .max(Integer::compareTo)
            .orElse(1);

    return ruleLevel > maxLevel;
  }
}
