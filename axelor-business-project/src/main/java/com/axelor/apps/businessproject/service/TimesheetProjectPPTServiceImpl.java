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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.hr.service.timesheet.TimesheetProjectPlanningTimeServiceImpl;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;

public class TimesheetProjectPPTServiceImpl extends TimesheetProjectPlanningTimeServiceImpl {

  @Inject
  public TimesheetProjectPPTServiceImpl(
      ProjectPlanningTimeRepository projectPlanningTimeRepository,
      TimesheetLineService timesheetLineService,
      AppBaseService appBaseService,
      UserHrService userHrService) {
    super(projectPlanningTimeRepository, timesheetLineService, appBaseService, userHrService);
  }

  @Override
  protected TimesheetLine createTimeSheetLineFromPPT(
      Timesheet timesheet, ProjectPlanningTime projectPlanningTime) throws AxelorException {
    TimesheetLine line = super.createTimeSheetLineFromPPT(timesheet, projectPlanningTime);
    if (ObjectUtils.notEmpty(projectPlanningTime.getProjectTask())
        && projectPlanningTime.getProjectTask().getInvoicingType()
            == ProjectTaskRepository.INVOICING_TYPE_TIME_SPENT) {
      line.setToInvoice(projectPlanningTime.getProjectTask().getToInvoice());
    }
    return line;
  }
}
