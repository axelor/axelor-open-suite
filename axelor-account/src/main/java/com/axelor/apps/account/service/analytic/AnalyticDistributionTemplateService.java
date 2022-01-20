package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.exception.AxelorException;

public interface AnalyticDistributionTemplateService {

  boolean validateTemplatePercentages(AnalyticDistributionTemplate analyticDistributionTemplate);

  AnalyticDistributionTemplate createDistributionTemplateFromAccount(Account account)
      throws AxelorException;
}
