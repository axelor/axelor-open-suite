/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.businessproject.service.TimesheetProjectServiceImpl;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TimesheetBusinessProductionServiceImpl extends TimesheetProjectServiceImpl {

  @Inject
  public TimesheetBusinessProductionServiceImpl(
      PriceListService priceListService,
      AppHumanResourceService appHumanResourceService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      ProjectRepository projectRepo,
      UserRepository userRepo,
      UserHrService userHrService,
      TimesheetLineService timesheetLineService,
      ProjectPlanningTimeRepository projectPlanningTimeRepository,
      TeamTaskRepository teamTaskRepository,
      TimesheetLineRepository timesheetLineRepo) {
    super(
        priceListService,
        appHumanResourceService,
        hrConfigService,
        templateMessageService,
        projectRepo,
        userRepo,
        userHrService,
        timesheetLineService,
        projectPlanningTimeRepository,
        teamTaskRepository,
        timesheetLineRepo);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void confirm(Timesheet timesheet) throws AxelorException {
    super.confirm(timesheet);
    AppProductionService appProductionService = Beans.get(AppProductionService.class);

    if (appProductionService.isApp("production")
        && appProductionService.getAppProduction().getManageBusinessProduction()) {
      Beans.get(OperationOrderTimesheetServiceImpl.class)
          .updateAllRealDuration(timesheet.getTimesheetLineList());
    }
  }

  @Override
  @Transactional
  public void validate(Timesheet timesheet) {
    super.validate(timesheet);
    AppProductionService appProductionService = Beans.get(AppProductionService.class);

    if (appProductionService.isApp("production")
        && appProductionService.getAppProduction().getManageBusinessProduction()) {
      Beans.get(OperationOrderTimesheetServiceImpl.class)
          .updateAllRealDuration(timesheet.getTimesheetLineList());
    }
  }

  @Override
  @Transactional
  public void refuse(Timesheet timesheet) {
    super.refuse(timesheet);
    AppProductionService appProductionService = Beans.get(AppProductionService.class);

    if (appProductionService.isApp("production")
        && appProductionService.getAppProduction().getManageBusinessProduction()) {
      Beans.get(OperationOrderTimesheetServiceImpl.class)
          .updateAllRealDuration(timesheet.getTimesheetLineList());
    }
  }

  @Override
  @Transactional
  public void cancel(Timesheet timesheet) {
    super.cancel(timesheet);
    AppProductionService appProductionService = Beans.get(AppProductionService.class);

    if (appProductionService.isApp("production")
        && appProductionService.getAppProduction().getManageBusinessProduction()) {
      Beans.get(OperationOrderTimesheetServiceImpl.class)
          .updateAllRealDuration(timesheet.getTimesheetLineList());
    }
  }

  @Override
  @Transactional
  public void draft(Timesheet timesheet) {
    super.draft(timesheet);
    AppProductionService appProductionService = Beans.get(AppProductionService.class);

    if (appProductionService.isApp("production")
        && appProductionService.getAppProduction().getManageBusinessProduction()) {
      Beans.get(OperationOrderTimesheetServiceImpl.class)
          .updateAllRealDuration(timesheet.getTimesheetLineList());
    }
  }
}
