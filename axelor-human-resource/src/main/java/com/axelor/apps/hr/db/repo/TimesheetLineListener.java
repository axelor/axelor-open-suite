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
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.inject.Beans;
import java.lang.invoke.MethodHandles;
import javax.persistence.PreRemove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimesheetLineListener {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @PreRemove
  protected void checkLinkedEntities(TimesheetLine timesheetLine) throws AxelorException {
    LOG.debug("Deleting {}", timesheetLine);
    checkTimer(timesheetLine);
    LOG.debug("Deleted {}", timesheetLine);
  }

  protected void checkTimer(TimesheetLine timesheetLine) throws AxelorException {
    LOG.debug("Checking linked timer");

    TSTimer timer = timesheetLine.getTimer();
    if (timer != null) {
      Beans.get(TSTimerRepository.class).remove(timer);
    }
  }
}
