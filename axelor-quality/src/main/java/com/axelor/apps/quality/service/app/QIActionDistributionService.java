package com.axelor.apps.quality.service.app;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.quality.db.QIActionDistribution;
import com.axelor.apps.quality.db.QIAnalysis;
import com.axelor.apps.quality.db.QITask;
import java.util.List;

public interface QIActionDistributionService {

  QIActionDistribution createQIActionDistribution(
      Company company, Integer recipient, Partner recepientPartner) throws AxelorException;

  QIActionDistribution createQIActionDistribution(
      QIAnalysis qiAnalysis, Company company, Partner responsiblePartner, List<QITask> qiTasks)
      throws AxelorException;
}
