package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.exception.AxelorException;

public interface AnalyticDistributionTemplateService {

  boolean validateTemplatePercentages(AnalyticDistributionTemplate analyticDistributionTemplate);

  public AnalyticDistributionTemplate personalizeAnalyticDistributionTemplate(
      AnalyticDistributionTemplate analyticDistributionTemplate) throws AxelorException;
}
