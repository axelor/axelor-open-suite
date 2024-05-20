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
package com.axelor.apps.hr.utils;

import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.db.JPA;
import java.math.BigDecimal;
import java.util.List;

public class ProjectPlanningTimeUtilsServiceImpl implements ProjectPlanningTimeUtilsService {

  @Override
  public BigDecimal getTaskPlannedHrs(ProjectTask task) {

    BigDecimal totalPlanned = BigDecimal.ZERO;
    if (task != null) {
      List<ProjectPlanningTime> plannings =
          JPA.all(ProjectPlanningTime.class).filter("self.projectTask = ?1", task).fetch();
      if (plannings != null) {
        totalPlanned =
            plannings.stream()
                .map(ProjectPlanningTime::getPlannedTime)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
      }
    }

    return totalPlanned;
  }

  @Override
  public BigDecimal getDurationForCustomer(ProjectTask projectTask) {
    String query =
        "SELECT SUM(self.durationForCustomer) FROM TimesheetLine AS self WHERE self.timesheet.statusSelect = :statusSelect AND self.projectTask = :projectTask";
    BigDecimal durationForCustomer =
        JPA.em()
            .createQuery(query, BigDecimal.class)
            .setParameter("statusSelect", TimesheetRepository.STATUS_VALIDATED)
            .setParameter("projectTask", projectTask)
            .getSingleResult();
    return durationForCustomer != null ? durationForCustomer : BigDecimal.ZERO;
  }
}
