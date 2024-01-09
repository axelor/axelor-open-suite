/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.timesheet.TimesheetWorkflowServiceImpl;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.inject.Beans;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TimesheetBusinessProductionWorkflowServiceImpl extends TimesheetWorkflowServiceImpl {

  @Inject
  public TimesheetBusinessProductionWorkflowServiceImpl(
      AppHumanResourceService appHumanResourceService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      TimesheetRepository timesheetRepository) {
    super(appHumanResourceService, hrConfigService, templateMessageService, timesheetRepository);
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
