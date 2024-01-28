package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.hr.service.timesheet.TimesheetProjectPlanningTimeServiceImpl;
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
      AppBaseService appBaseService) {
    super(projectPlanningTimeRepository, timesheetLineService, appBaseService);
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
