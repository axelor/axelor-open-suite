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
package com.axelor.apps.project.service.app;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.apps.project.db.ProjectConfig;
import com.axelor.apps.project.db.ProjectStatus;
import com.axelor.apps.project.db.repo.ProjectConfigRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.meta.loader.AppVersionService;
import com.axelor.studio.db.AppProject;
import com.axelor.studio.db.repo.AppProjectRepository;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;

@Singleton
public class AppProjectServiceImpl extends AppBaseServiceImpl implements AppProjectService {

  protected AppProjectRepository appProjectRepo;
  protected CompanyRepository companyRepo;
  protected ProjectConfigRepository projectConfigRepo;

  @Inject
  public AppProjectServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      AppSettingsStudioService appSettingsService,
      MetaModuleRepository metaModuleRepo,
      MetaFileRepository metaFileRepo,
      AppProjectRepository appProjectRepo,
      CompanyRepository companyRepo,
      ProjectConfigRepository projectConfigRepo) {
    super(appRepo, metaFiles, appVersionService, appSettingsService, metaModuleRepo, metaFileRepo);
    this.appProjectRepo = appProjectRepo;
    this.companyRepo = companyRepo;
    this.projectConfigRepo = projectConfigRepo;
  }

  @Override
  public AppProject getAppProject() {
    return appProjectRepo.all().fetchOne();
  }

  @Override
  @Transactional
  public void generateProjectConfigurations() {

    List<Company> companies = companyRepo.all().filter("self.projectConfig is null").fetch();

    for (Company company : companies) {
      ProjectConfig projectConfig = new ProjectConfig();
      projectConfig.setCompany(company);
      projectConfigRepo.save(projectConfig);
    }
  }

  @Override
  public ProjectStatus getCompletedProjectStatus() throws AxelorException {
    ProjectStatus projectStatus = getAppProject().getCompletedProjectStatus();
    if (projectStatus == null) {
      throw new AxelorException(
          getAppProject(),
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProjectExceptionMessage.PROJECT_CONFIG_COMPLETED_PROJECT_STATUS_MISSING));
    }
    return projectStatus;
  }
}
