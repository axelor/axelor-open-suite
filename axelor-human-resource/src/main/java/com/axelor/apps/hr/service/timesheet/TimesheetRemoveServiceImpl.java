package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.ListUtils;

public class TimesheetRemoveServiceImpl implements TimesheetRemoveService {

  protected TimesheetLineRepository timesheetLineRepository;

  @Inject
  public TimesheetRemoveServiceImpl(TimesheetLineRepository timesheetLineRepository) {
    this.timesheetLineRepository = timesheetLineRepository;
  }

  @Override
  @Transactional
  public void removeAfterToDateTimesheetLines(Timesheet timesheet) {

    List<TimesheetLine> removedTimesheetLines = new ArrayList<>();

    for (TimesheetLine timesheetLine : ListUtils.emptyIfNull(timesheet.getTimesheetLineList())) {
      if (timesheetLine.getDate().isAfter(timesheet.getToDate())) {
        removedTimesheetLines.add(timesheetLine);
        if (timesheetLine.getId() != null) {
          timesheetLineRepository.remove(timesheetLine);
        }
      }
    }
    timesheet.getTimesheetLineList().removeAll(removedTimesheetLines);
  }
}
