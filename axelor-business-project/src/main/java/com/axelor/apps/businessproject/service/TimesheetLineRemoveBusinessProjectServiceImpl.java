package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.businessproject.db.repo.ApprovalItemRepository;
import com.axelor.apps.businessproject.service.approvalitem.ApprovalItemManagementService;
import com.axelor.apps.businessproject.service.statuschange.ProjectStatusChangeService;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetLineRemoveServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import javax.persistence.PersistenceException;

public class TimesheetLineRemoveBusinessProjectServiceImpl extends TimesheetLineRemoveServiceImpl {

  @Inject protected ProjectStatusChangeService projectStatusChangeService;
  @Inject protected ApprovalItemManagementService approvalItemManagementService;

  @Inject
  public TimesheetLineRemoveBusinessProjectServiceImpl(
      TimesheetLineRepository timeSheetLineRepository) {
    super(timeSheetLineRepository);
  }

  @Override
  @Transactional
  public void removeTimesheetLine(TimesheetLine timesheetLine) {
    Project project = timesheetLine.getProject();
    approvalItemManagementService.deleteApprovalItem(
        timesheetLine, ApprovalItemRepository.TIMESHEET_LINE_APPROVAL_ITEM);

    super.removeTimesheetLine(timesheetLine);

    // When a timesheet line is deleted, the project's status should update to reflect that.
    try {
      Beans.get(ProjectStatusChangeService.class).updateProjectStatus(project);
    } catch (AxelorAlertException e) {
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
