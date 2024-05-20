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
package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.project.db.ProjectPlanningTime;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ProjectPlanningTimeService {

  public void addMultipleProjectPlanningTime(Map<String, Object> dataMap) throws AxelorException;

  void addSingleProjectPlanningTime(ProjectPlanningTime projectPlanningTime) throws AxelorException;

  public void removeProjectPlanningLines(List<Integer> projectPlanningLineIds);

  public void removeProjectPlanningLine(ProjectPlanningTime projectPlanningTime);

  void updateProjectPlanningTime(
      ProjectPlanningTime projectPlanningTime,
      LocalDateTime startDateTime,
      LocalDateTime endDateTime,
      String description);

  void updateLinkedEvent(ProjectPlanningTime projectPlanningTime);

  void deleteLinkedProjectPlanningTime(List<Long> ids);

  ProjectPlanningTime loadLinkedPlanningTime(ICalendarEvent event);

  BigDecimal computePlannedTime(ProjectPlanningTime projectPlanningTime) throws AxelorException;

  String computeDisplayTimeUnitDomain(ProjectPlanningTime projectPlanningTime);

  List<Long> computeAvailableDisplayTimeUnitIds(Unit unit);

  String computeDisplayPlannedTimeRestrictedDomain(ProjectPlanningTime projectPlanningTime)
      throws AxelorException;
}
