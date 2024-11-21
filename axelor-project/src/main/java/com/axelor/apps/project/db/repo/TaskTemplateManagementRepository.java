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

import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.TaskTemplateService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class TaskTemplateManagementRepository extends TaskTemplateRepository {

  @Override
  public TaskTemplate save(TaskTemplate taskTemplate) {

    if (taskTemplate.getVersion() != 0
        && Beans.get(TaskTemplateService.class)
            .isParentTaskTemplateCreatedLoop(taskTemplate, taskTemplate.getParentTaskTemplate())) {
      throw new PersistenceException(
          I18n.get(ProjectExceptionMessage.TASK_TEMPLATE_PARENT_TASK_CREATED_LOOP));
    }

    return super.save(taskTemplate);
  }
}
