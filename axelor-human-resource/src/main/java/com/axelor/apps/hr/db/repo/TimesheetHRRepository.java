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
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class TimesheetHRRepository extends TimesheetRepository {

  @Inject private TimesheetService timesheetService;
  @Inject private TimesheetLineService timesheetLineService;
  @Inject private ProjectRepository projectRepository;

  @Override
  public Timesheet save(Timesheet timesheet) {
    if (timesheet.getTimesheetLineList() != null) {
      for (TimesheetLine timesheetLine : timesheet.getTimesheetLineList())
        Beans.get(TimesheetLineHRRepository.class).computeFullName(timesheetLine);
    }
    return super.save(timesheet);
  }

  @Override
  public Map<String, Object> validate(Map<String, Object> json, Map<String, Object> context) {

    Map<String, Object> obj = super.validate(json, context);

    if (json.get("id") == null) {
      Timesheet timesheet = create(json);
      if (timesheet.getTimesheetLineList() == null || timesheet.getTimesheetLineList().isEmpty()) {
        timesheet.setTimesheetLineList(new ArrayList<TimesheetLine>());
        obj.put("timesheetLineList", timesheetService.createDefaultLines(timesheet));
      }
    }

    return obj;
  }

  @Override
  public void remove(Timesheet entity) {

    if (entity.getStatusSelect() == TimesheetRepository.STATUS_VALIDATED
        && entity.getTimesheetLineList() != null) {

      timesheetService.setProjectTaskTotalRealHrs(entity.getTimesheetLineList(), false);

      Map<Project, BigDecimal> projectTimeSpentMap =
          timesheetLineService.getProjectTimeSpentMap(entity.getTimesheetLineList());
      Iterator<Project> projectIterator = projectTimeSpentMap.keySet().iterator();

      while (projectIterator.hasNext()) {
        Project project = projectIterator.next();
        project.setTimeSpent(project.getTimeSpent().subtract(projectTimeSpentMap.get(project)));
        projectRepository.save(project);
      }
    }
    super.remove(entity);
  }
}
