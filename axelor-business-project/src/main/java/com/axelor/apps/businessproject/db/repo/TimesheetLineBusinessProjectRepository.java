package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.businessproject.service.approvalitem.ApprovalItemManagementService;
import com.axelor.apps.businessproject.service.statuschange.ProjectStatusChangeService;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineHRRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetLineComputeNameService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimesheetLineBusinessProjectRepository extends TimesheetLineHRRepository {

  private static final Logger log =
      LoggerFactory.getLogger(TimesheetLineBusinessProjectRepository.class);
  protected ApprovalItemManagementService approvalItemManagementService;

  @Inject
  public TimesheetLineBusinessProjectRepository(
      TimesheetLineComputeNameService timesheetLineComputeNameService,
      ApprovalItemManagementService approvalItemManagementService) {
    super(timesheetLineComputeNameService);
    this.approvalItemManagementService = approvalItemManagementService;
  }

  @Override
  public TimesheetLine save(TimesheetLine timesheetLine) {
    timesheetLine = super.save(timesheetLine);
    Integer version = timesheetLine.getVersion();

    // If a Timesheet Line is already validated, it should not have an Approval Item
    // As it is no longer relevant and should be deleted
    if (timesheetLine.getIsValidated()) {
      if (approvalItemManagementService.hasApprovalItem(
          timesheetLine, ApprovalItemRepository.TIMESHEET_LINE_APPROVAL_ITEM)) {
        approvalItemManagementService.deleteApprovalItem(
            timesheetLine, ApprovalItemRepository.TIMESHEET_LINE_APPROVAL_ITEM);
      }
    } else {
      approvalItemManagementService.createApprovalItem(
          timesheetLine,
          timesheetLine.getProject(),
          timesheetLine.getEmployee(),
          ApprovalItemRepository.TIMESHEET_LINE_APPROVAL_ITEM,
          timesheetLine.getComments(),
          "Hours",
          timesheetLine.getStartTime(),
          timesheetLine.getDuration());
    }

    // When a timesheet line is created, validated or canceled, the project's status
    // should update to reflect this change.
    try {
      Beans.get(ProjectStatusChangeService.class).updateProjectStatus(timesheetLine.getProject());
    } catch (AxelorAlertException e) {
      throw new PersistenceException(e.getMessage(), e);
    }
    return timesheetLine;
  }

  @Override
  public void remove(TimesheetLine timesheetLine) {
    super.remove(timesheetLine);

    approvalItemManagementService.deleteApprovalItem(
        timesheetLine, ApprovalItemRepository.TIMESHEET_LINE_APPROVAL_ITEM);
  }
}
