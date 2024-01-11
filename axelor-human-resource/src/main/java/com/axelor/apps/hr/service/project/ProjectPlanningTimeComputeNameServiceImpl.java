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

import com.axelor.apps.base.service.DateService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import com.google.inject.Inject;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectPlanningTimeComputeNameServiceImpl
    implements ProjectPlanningTimeComputeNameService {

  protected DateService dateService;

  @Inject
  public ProjectPlanningTimeComputeNameServiceImpl(DateService dateService) {
    this.dateService = dateService;
  }

  @Override
  public String computeProjectPlanningTimeFullname(
      Employee employee, Project project, LocalDate date) {
    String fullName = "";

    try {
      if (employee != null && employee.getName() != null) {
        fullName += employee.getName();
      }

      if (project != null && project.getCode() != null) {
        fullName += "-" + project.getCode();
      }

      String dateStr = date.format(dateService.getDateFormat());
      if (!fullName.isEmpty()) {
        fullName += "-" + dateStr;
      } else {
        fullName = dateStr;
      }
    } catch (Exception e) {
      Logger logger = LoggerFactory.getLogger(getClass());
      logger.error(e.getMessage());
    }
    return fullName;
  }
}
