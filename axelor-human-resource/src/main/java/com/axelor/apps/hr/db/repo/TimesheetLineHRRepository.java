/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.exception.service.TraceBackService;
import javax.persistence.PersistenceException;

public class TimesheetLineHRRepository extends TimesheetLineRepository {

  @Override
  public TimesheetLine save(TimesheetLine timesheetLine) {
    try {
      computeFullName(timesheetLine);

      return super.save(timesheetLine);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  public void computeFullName(TimesheetLine timesheetLine) {

    timesheetLine.setFullName(
        timesheetLine.getTimesheet().getFullName()
            + " "
            + timesheetLine.getDate()
            + " "
            + timesheetLine.getId());
  }
}
