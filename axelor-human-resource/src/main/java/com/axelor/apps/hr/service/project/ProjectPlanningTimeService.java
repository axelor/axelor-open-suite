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
package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.PlannedTimeValue;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ProjectPlanningTimeService {

  public BigDecimal getTaskPlannedHrs(ProjectTask projectTask);

  public BigDecimal getProjectPlannedHrs(Project project);

  void addSingleProjectPlanningTime(ProjectPlanningTime projectPlanningTime) throws AxelorException;

  public void removeProjectPlanningLines(List<Integer> projectPlanningLineIds);

  public void removeProjectPlanningLine(ProjectPlanningTime projectPlanningTime);

  public BigDecimal getDurationForCustomer(ProjectTask projectTask);

  void updateProjectPlanningTime(
      ProjectPlanningTime projectPlanningTime,
      LocalDateTime startDateTime,
      LocalDateTime endDateTime,
      String description);

  void updateLinkedEvent(ProjectPlanningTime projectPlanningTime);

  void deleteLinkedProjectPlanningTime(List<Long> ids);

  ProjectPlanningTime loadLinkedPlanningTime(ICalendarEvent event);

  BigDecimal computePlannedTime(ProjectPlanningTime projectPlanningTime) throws AxelorException;

  String computeDisplayTimeUnitDomain(ProjectPlanningTime projectPlanningTime)
      throws AxelorException;

  List<Long> computeAvailableDisplayTimeUnitIds(Unit unit);

  String computeDisplayPlannedTimeRestrictedDomain(ProjectPlanningTime projectPlanningTime)
      throws AxelorException;

  BigDecimal getDefaultPlanningTime(ProjectPlanningTime projectPlanningTime) throws AxelorException;

  PlannedTimeValue getDefaultPlanningRestrictedTime(ProjectPlanningTime projectPlanningTime)
      throws AxelorException;

  List<ProjectPlanningTime> getProjectPlanningTimeIdList(
      Employee employee, LocalDate fromDate, LocalDate toDate);
}
