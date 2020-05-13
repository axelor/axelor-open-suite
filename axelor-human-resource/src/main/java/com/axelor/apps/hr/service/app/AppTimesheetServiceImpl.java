package com.axelor.apps.hr.service.app;

import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class AppTimesheetServiceImpl implements AppTimesheetService {
  protected TimesheetRepository timesheetRepo;

  @Inject
  public AppTimesheetServiceImpl(TimesheetRepository timesheetRepo) {
    this.timesheetRepo = timesheetRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void switchTimesheetEditors(Boolean state) {
    List<Timesheet> timesheets;
    Query<Timesheet> query = timesheetRepo.all().order("id");
    int offset = 0;
    while (!(timesheets = query.fetch(AbstractBatch.FETCH_LIMIT, offset)).isEmpty()) {
      for (Timesheet timesheet : timesheets) {
        offset++;
        if (timesheet.getShowEditor() != state) {
          timesheet.setShowEditor(state);
          timesheetRepo.save(timesheet);
        }
      }
      JPA.clear();
    }
  }
}
