/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.service.app;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.apps.crm.db.CrmConfig;
import com.axelor.apps.crm.db.LeadStatus;
import com.axelor.apps.crm.db.OpportunityStatus;
import com.axelor.apps.crm.db.repo.CrmConfigRepository;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.app.service.AppVersionService;
import com.axelor.studio.db.AppCrm;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class AppCrmServiceImpl extends AppBaseServiceImpl implements AppCrmService {

  protected CompanyRepository companyRepo;

  protected CrmConfigRepository crmConfigRepo;

  @Inject
  public AppCrmServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      MetaModelRepository metaModelRepo,
      AppSettingsStudioService appSettingsStudioService,
      CompanyRepository companyRepo,
      CrmConfigRepository crmConfigRepo) {
    super(appRepo, metaFiles, appVersionService, metaModelRepo, appSettingsStudioService);
    this.companyRepo = companyRepo;
    this.crmConfigRepo = crmConfigRepo;
  }

  @Override
  @Transactional
  public void generateCrmConfigurations() {

    List<Company> companies = companyRepo.all().filter("self.crmConfig is null").fetch();

    for (Company company : companies) {
      CrmConfig crmConfig = new CrmConfig();
      crmConfig.setCompany(company);
      crmConfigRepo.save(crmConfig);
    }
  }

  @Override
  public AppCrm getAppCrm() {
    return Query.of(AppCrm.class).fetchOne();
  }

  @Override
  public LeadStatus getLostLeadStatus() throws AxelorException {
    AppCrm appCrm = getAppCrm();
    LeadStatus lostLeadStatus = appCrm.getLostLeadStatus();

    if (lostLeadStatus == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(CrmExceptionMessage.CRM_LOST_LEAD_STATUS_MISSING));
    }
    return lostLeadStatus;
  }

  @Override
  public LeadStatus getConvertedLeadStatus() throws AxelorException {
    AppCrm appCrm = getAppCrm();

    LeadStatus convertedLeadStatus = appCrm.getConvertedLeadStatus();
    if (convertedLeadStatus == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(CrmExceptionMessage.CRM_CONVERTED_LEAD_STATUS_MISSING));
    }
    return convertedLeadStatus;
  }

  @Override
  public OpportunityStatus getClosedWinOpportunityStatus() throws AxelorException {
    OpportunityStatus closedWinOpportunityStatus = getAppCrm().getClosedWinOpportunityStatus();
    if (closedWinOpportunityStatus == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(CrmExceptionMessage.CRM_CLOSED_WIN_OPPORTUNITY_STATUS_MISSING));
    }
    return closedWinOpportunityStatus;
  }

  @Override
  public OpportunityStatus getClosedLostOpportunityStatus() throws AxelorException {
    OpportunityStatus closedLostOpportunityStatus = getAppCrm().getClosedLostOpportunityStatus();

    if (closedLostOpportunityStatus == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(CrmExceptionMessage.CRM_CLOSED_LOST_OPPORTUNITY_STATUS_MISSING));
    }

    return closedLostOpportunityStatus;
  }

  @Override
  public OpportunityStatus getSalesPropositionStatus() throws AxelorException {
    OpportunityStatus salesPropositionStatus = getAppCrm().getSalesPropositionStatus();

    if (salesPropositionStatus == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(CrmExceptionMessage.CRM_SALES_PROPOSITION_STATUS_MISSING));
    }

    return salesPropositionStatus;
  }
}
