/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.app;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.HRConfigRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.app.service.AppVersionService;
import com.axelor.studio.db.AppHumanResource;
import com.axelor.studio.db.repo.AppHumanResourceRepository;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class AppHumanResourceServiceImpl extends AppBaseServiceImpl
    implements AppHumanResourceService {

  protected AppHumanResourceRepository appHumanResourceRepository;
  protected CompanyRepository companyRepo;
  protected HRConfigRepository hrConfigRepo;
  protected TimesheetRepository timesheetRepository;

  @Inject
  public AppHumanResourceServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      MetaModelRepository metaModelRepo,
      AppSettingsStudioService appSettingsStudioService,
      AppHumanResourceRepository appHumanResourceRepository,
      CompanyRepository companyRepo,
      HRConfigRepository hrConfigRepo,
      TimesheetRepository timesheetRepository) {
    super(appRepo, metaFiles, appVersionService, metaModelRepo, appSettingsStudioService);
    this.appHumanResourceRepository = appHumanResourceRepository;
    this.companyRepo = companyRepo;
    this.hrConfigRepo = hrConfigRepo;
    this.timesheetRepository = timesheetRepository;
  }

  @Override
  public AppHumanResource getAppHumanResource() {
    return appHumanResourceRepository.all().fetchOne();
  }

  @Override
  @Transactional
  public void generateHrConfigurations() {
    List<Company> companies = companyRepo.all().filter("self.hrConfig is null").fetch();

    for (Company company : companies) {
      HRConfig hrConfig = new HRConfig();
      hrConfig.setCompany(company);
      hrConfigRepo.save(hrConfig);
    }
  }

  @Override
  @Transactional
  public void switchTimesheetEditors(Boolean state) {
    List<Timesheet> timesheets;
    Query<Timesheet> query = timesheetRepository.all().order("id");
    int offset = 0;
    while (!(timesheets = query.fetch(AbstractBatch.FETCH_LIMIT, offset)).isEmpty()) {
      for (Timesheet timesheet : timesheets) {
        offset++;
        if (timesheet.getShowEditor() != state) {
          timesheet.setShowEditor(state);
          timesheetRepository.save(timesheet);
        }
      }
      JPA.clear();
    }
  }
}
