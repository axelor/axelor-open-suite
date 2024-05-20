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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimesheetLineCreateDefaultServiceImpl implements TimesheetLineCreateDefaultService {

  protected UserHrService userHrService;

  @Inject
  public TimesheetLineCreateDefaultServiceImpl(UserHrService userHrService) {
    this.userHrService = userHrService;
  }

  @Override
  public List<Map<String, Object>> createDefaultLines(Timesheet timesheet) {
    List<Map<String, Object>> lines = new ArrayList<>();
    User user = timesheet.getEmployee().getUser();
    if (user == null || timesheet.getFromDate() == null) {
      return lines;
    }
    Product product = userHrService.getTimesheetProduct(timesheet.getEmployee());

    if (product == null) {
      return lines;
    }
    List<Project> projects =
        JPA.all(Project.class)
            .filter(
                "self.membersUserSet.id = ?1 and "
                    + "self.imputable = true "
                    + "and self.projectStatus.isCompleted = false "
                    + "and self.isShowTimeSpent = true",
                user.getId())
            .fetch();

    for (Project project : projects) {
      TimesheetLine line =
          Beans.get(TimesheetLineCreateService.class)
              .createTimesheetLine(
                  project,
                  product,
                  timesheet.getEmployee(),
                  timesheet.getFromDate(),
                  timesheet,
                  new BigDecimal(0),
                  null);
      lines.add(Mapper.toMap(line));
    }
    return lines;
  }
}
