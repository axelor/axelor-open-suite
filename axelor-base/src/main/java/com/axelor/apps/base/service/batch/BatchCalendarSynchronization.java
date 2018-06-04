package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ICalendar;
import com.axelor.apps.base.db.repo.ICalendarRepository;
import com.axelor.apps.base.ical.ICalendarService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.google.inject.Inject;
import java.util.List;

public class BatchCalendarSynchronization extends AbstractBatch {

  @Inject ICalendarService iCalendarService;

  @Inject ICalendarRepository repo;

  @Override
  protected void process() {
    final Company company = batch.getBaseBatch().getCompany();
    ;
    final List<ICalendar> calendars =
        repo.all()
            .filter("self.user.activeCompany = :company AND self.isValid = TRUE")
            .bind("company", company)
            .fetch();

    for (ICalendar calendar : calendars) {
      try {
        iCalendarService.sync(
            calendar,
            batch.getBaseBatch().getAllEvents(),
            batch.getBaseBatch().getSynchronizationDuration());
        incrementDone();
      } catch (Exception e) {
        e.printStackTrace();
        incrementAnomaly();
      }
    }
  }
}
