package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.exception.service.TraceBackService;
import java.math.BigDecimal;
import javax.persistence.PersistenceException;

public class AccountAnalyticDistributionTemplateRepository
    extends AnalyticDistributionTemplateRepository {

  @Override
  public AnalyticDistributionTemplate save(
      AnalyticDistributionTemplate analyticDistributionTemplate) {
    try {
      if (analyticDistributionTemplate.getId() == null) {
        return super.save(analyticDistributionTemplate);
      }
      if (analyticDistributionTemplate.getAnalyticDistributionLineList().size() == 1) {
        analyticDistributionTemplate
            .getAnalyticDistributionLineList()
            .get(0)
            .setPercentage(new BigDecimal(100));
      }
      return super.save(analyticDistributionTemplate);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e);
    }
  }
}
