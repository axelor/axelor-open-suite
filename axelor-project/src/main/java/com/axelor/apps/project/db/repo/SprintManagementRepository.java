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
package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SprintManagementRepository extends SprintRepository {

  @Override
  public Sprint save(Sprint sprint) {

    List<ProjectTask> projectTaskList = sprint.getProjectTaskList();

    if (CollectionUtils.isNotEmpty(projectTaskList)) {
      BigDecimal totalEstimatedTime =
          projectTaskList.stream()
              .map(ProjectTask::getBudgetedTime)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      sprint.setTotalEstimatedTime(totalEstimatedTime);
    } else {
      sprint.setTotalEstimatedTime(BigDecimal.ZERO);
    }

    return super.save(sprint);
  }
}
