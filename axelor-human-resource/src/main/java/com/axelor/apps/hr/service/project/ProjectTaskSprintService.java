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
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import java.util.Set;

public interface ProjectTaskSprintService {

  void createOrMovePlanification(ProjectTask projectTask) throws AxelorException;

  Sprint validateConfigAndSprint(ProjectTask projectTask);

  Set<ProjectPlanningTime> getProjectPlanningTimeOnOldSprint(
      ProjectTask projectTask, Sprint savedSprint);

  void moveProjectPlanningTime(ProjectPlanningTime projectPlanningTime, ProjectTask projectTask)
      throws AxelorException;

  void createProjectPlanningTime(ProjectTask projectTask) throws AxelorException;

  Sprint getOldActiveSprint(ProjectTask projectTask);
}
