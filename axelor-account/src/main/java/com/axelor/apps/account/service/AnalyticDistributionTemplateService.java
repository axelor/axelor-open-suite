package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.exception.AxelorException;

public interface AnalyticDistributionTemplateService {

  void validateTemplatePercentages(AnalyticDistributionTemplate analyticDistributionTemplate)
      throws AxelorException;

  AnalyticDistributionTemplate createDistributionTemplateFromAccount(Account account)
      throws AxelorException;
}
