/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
