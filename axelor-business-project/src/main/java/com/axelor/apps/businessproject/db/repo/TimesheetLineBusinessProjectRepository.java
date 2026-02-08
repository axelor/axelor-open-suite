package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.businessproject.service.statuschange.ProjectStatusChangeService;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineHRRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetLineComputeNameService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class TimesheetLineBusinessProjectRepository extends TimesheetLineHRRepository {

  @Inject
  public TimesheetLineBusinessProjectRepository(
      TimesheetLineComputeNameService timesheetLineComputeNameService) {
    super(timesheetLineComputeNameService);
  }

  @Override
  public TimesheetLine save(TimesheetLine timesheetLine) {
    TimesheetLine line = super.save(timesheetLine);

    // When a timesheet line is created, validated or canceled, the project's status
    // should update to reflect this change.
    try {
      Beans.get(ProjectStatusChangeService.class).updateProjectStatus(line.getProject());
    } catch (AxelorAlertException e) {
      throw new PersistenceException(e.getMessage(), e);
    }
    return line;
  }
}
