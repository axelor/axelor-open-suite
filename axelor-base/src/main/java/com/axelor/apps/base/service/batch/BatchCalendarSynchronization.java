/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ICalendar;
import com.axelor.apps.base.db.repo.BatchRepository;
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

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_BASE_BATCH);
  }
}
