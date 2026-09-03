/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.apps.project.db.ProjectBatch;
import com.axelor.apps.project.db.repo.ProjectBatchRepository;
import com.axelor.db.Model;
import com.axelor.inject.Beans;

public class ProjectBatchService extends AbstractBatchService {

  @Override
  protected Class<? extends Model> getModelClass() {
    return ProjectBatch.class;
  }

  @Override
  public Batch run(Model model) throws AxelorException {

    Batch batch;
    ProjectBatch projectBatch = (ProjectBatch) model;

    switch (projectBatch.getActionSelect()) {
      case ProjectBatchRepository.ACTION_NOTIFY_TASKS:
        batch = notifyTasks(projectBatch);
        break;
      default:
        batch = removeTaskStatus(projectBatch);
        break;
    }

    return batch;
  }

  public Batch removeTaskStatus(ProjectBatch projectBatch) {
    return Beans.get(BatchRemoveTaskStatusService.class).run(projectBatch);
  }

  public Batch notifyTasks(ProjectBatch projectBatch) {
    return Beans.get(BatchProjectTaskNotification.class).run(projectBatch);
  }
}
