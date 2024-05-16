package com.axelor.apps.businessproject.service.analytic;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.businessproject.db.BusinessProjectConfig;
import com.axelor.apps.businessproject.service.config.BusinessProjectConfigService;
import com.axelor.apps.project.db.Project;
import com.axelor.auth.db.User;
import com.google.inject.Inject;

public class ProjectAnalyticTemplateServiceImpl implements ProjectAnalyticTemplateService {

  protected BusinessProjectConfigService businessProjectConfigService;
  protected AccountingSituationService accountingSituationService;
  protected AnalyticToolService analyticToolService;

  @Inject
  public ProjectAnalyticTemplateServiceImpl(
      BusinessProjectConfigService businessProjectConfigService,
      AccountingSituationService accountingSituationService,
      AnalyticToolService analyticToolService) {
    this.businessProjectConfigService = businessProjectConfigService;
    this.accountingSituationService = accountingSituationService;
    this.analyticToolService = analyticToolService;
  }

  @Override
  public AnalyticDistributionTemplate getDefaultAnalyticDistributionTemplate(Project project)
      throws AxelorException {
    if (project == null || project.getCompany() == null) {
      return null;
    }

    Company company = project.getCompany();

    BusinessProjectConfig businessProjectConfig =
        businessProjectConfigService.getBusinessProjectConfig(company);

    if (businessProjectConfig.getUseAssignedToAnalyticDistribution()) {
      User assignedToUser = project.getAssignedTo();
      if (assignedToUser != null && assignedToUser.getEmployee() != null) {
        return assignedToUser.getEmployee().getAnalyticDistributionTemplate();
      }
    }

    Partner partner = project.getClientPartner();

    if (partner == null) {
      return null;
    }
    AccountingSituation accountingSituation =
        accountingSituationService.getAccountingSituation(partner, company);
    return accountingSituation != null
        ? accountingSituation.getAnalyticDistributionTemplate()
        : null;
  }

  @Override
  public boolean isAnalyticDistributionTemplateRequired(Project project) throws AxelorException {
    return project.getCompany() != null
        && analyticToolService.isManageAnalytic(project.getCompany())
        && businessProjectConfigService
            .getBusinessProjectConfig(project.getCompany())
            .getIsAnalyticDistributionRequired();
  }
}
