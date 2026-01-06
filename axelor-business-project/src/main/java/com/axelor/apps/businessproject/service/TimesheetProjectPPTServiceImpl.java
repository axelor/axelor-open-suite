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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Site;
import com.axelor.apps.base.db.repo.UnitConversionRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.leave.LeaveRequestService;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.hr.service.timesheet.TimesheetProjectPlanningTimeServiceImpl;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.UnitConversionForProjectService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TimesheetProjectPPTServiceImpl extends TimesheetProjectPlanningTimeServiceImpl {

  @Inject
  public TimesheetProjectPPTServiceImpl(
      ProjectPlanningTimeRepository projectPlanningTimeRepository,
      TimesheetLineService timesheetLineService,
      AppBaseService appBaseService,
      UserHrService userHrService,
      UnitConversionRepository unitConversionRepository,
      UnitConversionForProjectService unitConversionForProjectService,
      WeeklyPlanningService weeklyPlanningService,
      LeaveRequestService leaveRequestService,
      PublicHolidayHrService publicHolidayHrService) {
    super(
        projectPlanningTimeRepository,
        timesheetLineService,
        appBaseService,
        userHrService,
        unitConversionRepository,
        unitConversionForProjectService,
        weeklyPlanningService,
        leaveRequestService,
        publicHolidayHrService);
  }

  @Override
  protected TimesheetLine createTimeSheetLineFromPPT(
      Timesheet timesheet,
      ProjectPlanningTime projectPlanningTime,
      LocalDate date,
      BigDecimal plannedTime,
      Project project,
      Product product,
      ProjectTask projectTask,
      Site site)
      throws AxelorException {
    TimesheetLine line =
        super.createTimeSheetLineFromPPT(
            timesheet, projectPlanningTime, date, plannedTime, project, product, projectTask, site);
    if (ObjectUtils.notEmpty(projectPlanningTime.getProjectTask())
        && projectPlanningTime.getProjectTask().getInvoicingType()
            == ProjectTaskRepository.INVOICING_TYPE_TIME_SPENT) {
      line.setToInvoice(projectPlanningTime.getProjectTask().getToInvoice());
    }
    return line;
  }
}
