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
package com.axelor.apps.project.service.batch;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectBatch;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskStatus;
import java.util.Set;

public interface ProjectBatchInitService {
  ProjectBatch initializeProjectBatchWithProjects(
      Integer actionSelect, Set<Project> projectSet, Set<TaskStatus> taskStatusSet);

  ProjectBatch initializeProjectBatchWithCategories(
      Integer actionSelect,
      Set<ProjectTaskCategory> projectTaskCategorySet,
      Set<TaskStatus> taskStatusSet);
}
