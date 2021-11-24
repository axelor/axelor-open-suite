package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;

public interface AnalyticDistributionTemplateService {

  boolean validateTemplatePercentages(AnalyticDistributionTemplate analyticDistributionTemplate);

  public AnalyticDistributionTemplate personalizeAnalyticDistributionTemplate(
      AnalyticDistributionTemplate analyticDistributionTemplate, Company company)
      throws AxelorException;

  AnalyticDistributionTemplate createDistributionTemplateFromAccount(Account account)
      throws AxelorException;

  void checkAnalyticAccounts(AnalyticDistributionTemplate analyticDistributionTemplate)
      throws AxelorException;
}
