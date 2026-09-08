/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.auth.db.User;
import com.axelor.studio.db.AppTimesheet;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TimesheetBusinessServiceImpl implements TimesheetBusinessService {

  protected AppHumanResourceService appHumanResourceService;

  @Inject
  public TimesheetBusinessServiceImpl(AppHumanResourceService appHumanResourceService) {
    this.appHumanResourceService = appHumanResourceService;
  }

  @Override
  public Map<String, Object> computeShowActivity(Timesheet timesheet) {
    AppTimesheet appTimesheet = appHumanResourceService.getAppTimesheet();
    boolean showActivity =
        Optional.ofNullable(timesheet)
            .map(Timesheet::getEmployee)
            .map(Employee::getUser)
            .map(User::getActiveCompany)
            .map(Company::getHrConfig)
            .map(
                hrConfig ->
                    hrConfig != null
                        && !hrConfig.getUseUniqueProductForTimesheet()
                        && appTimesheet.getEnableActivity())
            .orElse(true);
    int dailyLimit = appTimesheet.getDailyLimit();

    Map<String, Object> values = new HashMap<>();
    values.put("$showActivity", showActivity);
    values.put("$dailyLimit", dailyLimit);
    return values;
  }
}
