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
package com.axelor.apps.project.service;

import com.axelor.apps.project.db.TaskStatus;
import com.axelor.common.ObjectUtils;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectTaskToolServiceImpl implements ProjectTaskToolService {

  public ProjectTaskToolServiceImpl() {}

  @Override
  public Optional<TaskStatus> getCompletedTaskStatus(
      TaskStatus defaultTaskStatus, Set<TaskStatus> taskStatusSet) {
    Optional<TaskStatus> completedTaskStatus = Optional.empty();

    if (!ObjectUtils.isEmpty(taskStatusSet)) {
      completedTaskStatus = Optional.ofNullable(defaultTaskStatus);
      if (completedTaskStatus.isEmpty() || !taskStatusSet.contains(completedTaskStatus.get())) {
        completedTaskStatus = Optional.empty();
      }

      if (completedTaskStatus.isEmpty()) {
        List<TaskStatus> completedTaskStatusList =
            taskStatusSet.stream().filter(TaskStatus::getIsCompleted).collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(completedTaskStatusList) && completedTaskStatusList.size() == 1) {
          return Optional.ofNullable(completedTaskStatusList.get(0));
        }
      }
    }
    return completedTaskStatus;
  }
}
