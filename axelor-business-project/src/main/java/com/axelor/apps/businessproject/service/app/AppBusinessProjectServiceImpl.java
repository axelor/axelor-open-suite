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
package com.axelor.apps.businessproject.service.app;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.apps.businessproject.db.BusinessProjectConfig;
import com.axelor.apps.businessproject.db.repo.BusinessProjectConfigRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.meta.loader.AppVersionService;
import com.axelor.studio.db.AppBusinessProject;
import com.axelor.studio.db.repo.AppBusinessProjectRepository;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;

@Singleton
public class AppBusinessProjectServiceImpl extends AppBaseServiceImpl
    implements AppBusinessProjectService {

  protected AppBusinessProjectRepository appBusinessProjectRepo;
  protected BusinessProjectConfigRepository businessProjectConfigRepository;
  protected CompanyRepository companyRepository;
  protected AppProjectService appProjectService;

  @Inject
  public AppBusinessProjectServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      AppSettingsStudioService appSettingsService,
      MetaModuleRepository metaModuleRepo,
      MetaFileRepository metaFileRepo,
      AppBusinessProjectRepository appBusinessProjectRepo,
      BusinessProjectConfigRepository businessProjectConfigRepository,
      CompanyRepository companyRepository,
      AppProjectService appProjectService) {
    super(appRepo, metaFiles, appVersionService, appSettingsService, metaModuleRepo, metaFileRepo);
    this.appBusinessProjectRepo = appBusinessProjectRepo;
    this.businessProjectConfigRepository = businessProjectConfigRepository;
    this.companyRepository = companyRepository;
    this.appProjectService = appProjectService;
  }

  @Override
  public AppBusinessProject getAppBusinessProject() {
    return appBusinessProjectRepo.all().fetchOne();
  }

  @Override
  public PrintingTemplate getInvoicingAnnexPrintTemplate() throws AxelorException {
    PrintingTemplate invoicingAnnexPrintTemplate =
        getAppBusinessProject().getInvoicingAnnexPrintTemplate();
    if (invoicingAnnexPrintTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
    }
    return invoicingAnnexPrintTemplate;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateBusinessProjectConfigurations() {

    List<Company> companies =
        companyRepository.all().filter("self.businessProjectConfig is null").fetch();

    for (Company company : companies) {
      BusinessProjectConfig businessProjectConfig = new BusinessProjectConfig();
      businessProjectConfig.setCompany(company);
      businessProjectConfigRepository.save(businessProjectConfig);
    }
  }
}
