package com.axelor.apps.businessproject.service.analytic;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;

public interface ProjectAnalyticTemplateService {
  AnalyticDistributionTemplate getDefaultAnalyticDistributionTemplate(Project project)
      throws AxelorException;

  boolean isAnalyticDistributionTemplateRequired(Project project) throws AxelorException;
}
