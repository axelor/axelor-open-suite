/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.project;

import com.axelor.apps.project.db.Project;
import com.axelor.exception.AxelorException;
import com.axelor.team.db.TeamTask;
import java.math.BigDecimal;
import java.util.Map;

public interface ProjectPlanningTimeService {

  public BigDecimal getTaskPlannedHrs(TeamTask teamTask);

  public BigDecimal getTaskRealHrs(TeamTask teamTask);

  public BigDecimal getProjectPlannedHrs(Project project);

  public BigDecimal getProjectRealHrs(Project project);

  public void addMultipleProjectPlanningTime(Map<String, Object> dataMap) throws AxelorException;
}
