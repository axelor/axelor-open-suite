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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

/**
 * @author axelor
 */
public class TimesheetServiceImpl implements TimesheetService {

  @Inject
  public TimesheetServiceImpl() {}

  @Override
  public String getPeriodTotalConvertTitle(Timesheet timesheet) {
    String title = "";
    if (timesheet != null) {
      if (timesheet.getTimeLoggingPreferenceSelect() != null) {
        title = timesheet.getTimeLoggingPreferenceSelect();
      }
    } else {
      title = Beans.get(AppBaseService.class).getAppBase().getTimeLoggingPreferenceSelect();
    }
    switch (title) {
      case EmployeeRepository.TIME_PREFERENCE_DAYS:
        return I18n.get("Days");
      case EmployeeRepository.TIME_PREFERENCE_MINUTES:
        return I18n.get("Minutes");
      default:
        return I18n.get("Hours");
    }
  }
}
