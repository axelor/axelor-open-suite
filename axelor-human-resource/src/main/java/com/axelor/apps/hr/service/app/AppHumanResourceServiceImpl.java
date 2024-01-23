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
package com.axelor.apps.hr.service.app;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.repo.HRConfigRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.app.service.AppVersionService;
import com.axelor.studio.db.AppExpense;
import com.axelor.studio.db.AppLeave;
import com.axelor.studio.db.AppTimesheet;
import com.axelor.studio.db.repo.AppExpenseRepository;
import com.axelor.studio.db.repo.AppLeaveRepository;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.db.repo.AppTimesheetRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class AppHumanResourceServiceImpl extends AppBaseServiceImpl
    implements AppHumanResourceService {

  private AppTimesheetRepository appTimesheetRepo;
  private AppLeaveRepository appLeaveRepo;
  private AppExpenseRepository appExpenseRepo;

  protected CompanyRepository companyRepo;

  protected HRConfigRepository hrConfigRepo;

  @Inject
  public AppHumanResourceServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      MetaModelRepository metaModelRepo,
      AppSettingsStudioService appSettingsStudioService,
      AppTimesheetRepository appTimesheetRepo,
      AppLeaveRepository appLeaveRepo,
      AppExpenseRepository appExpenseRepo,
      CompanyRepository companyRepo,
      HRConfigRepository hrConfigRepo) {
    super(appRepo, metaFiles, appVersionService, metaModelRepo, appSettingsStudioService);
    this.appTimesheetRepo = appTimesheetRepo;
    this.appLeaveRepo = appLeaveRepo;
    this.appExpenseRepo = appExpenseRepo;
    this.companyRepo = companyRepo;
    this.hrConfigRepo = hrConfigRepo;
  }

  @Override
  public AppTimesheet getAppTimesheet() {
    return appTimesheetRepo.all().fetchOne();
  }

  @Override
  public AppLeave getAppLeave() {
    return appLeaveRepo.all().fetchOne();
  }

  @Override
  public AppExpense getAppExpense() {
    return appExpenseRepo.all().fetchOne();
  }

  @Override
  public void getHrmAppSettings(ActionRequest request, ActionResponse response) {

    try {

      Map<String, Object> map = new HashMap<>();

      map.put("hasInvoicingAppEnable", isApp("invoice"));
      map.put("hasLeaveAppEnable", isApp("leave"));
      map.put("hasExpenseAppEnable", isApp("expense"));
      map.put("hasTimesheetAppEnable", isApp("timesheet"));
      map.put("hasProjectAppEnable", isApp("project"));

      response.setData(map);
      response.setTotal(map.size());

    } catch (Exception e) {
      e.printStackTrace();
      response.setException(e);
    }
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
}
