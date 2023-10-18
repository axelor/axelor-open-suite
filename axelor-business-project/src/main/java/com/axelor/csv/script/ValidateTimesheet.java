/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Map;

public class ValidateTimesheet {

  protected final TimesheetRepository timesheetRepository;

  protected final TimesheetService timesheetService;

  @Inject
  public ValidateTimesheet(
      TimesheetRepository timesheetRepository, TimesheetService timesheetService) {
    this.timesheetRepository = timesheetRepository;
    this.timesheetService = timesheetService;
  }

  @Transactional
  public Object validateTimesheet(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof Timesheet;

    Timesheet timesheet = (Timesheet) bean;

    if (timesheet.getStatusSelect() == TimesheetRepository.STATUS_VALIDATED) {
      timesheetService.validate(timesheet);
      timesheetRepository.save(timesheet);
    }

    return timesheet;
  }
}
