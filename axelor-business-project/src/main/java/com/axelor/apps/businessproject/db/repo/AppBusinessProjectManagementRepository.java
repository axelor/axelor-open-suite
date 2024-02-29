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
package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppBusinessProject;
import com.axelor.studio.db.repo.AppBusinessProjectRepository;
import com.google.common.base.Strings;
import javax.persistence.PersistenceException;

public class AppBusinessProjectManagementRepository extends AppBusinessProjectRepository {

  @Override
  public AppBusinessProject save(AppBusinessProject entity) {
    try {
      if (!Strings.isNullOrEmpty(entity.getExcludeTaskInvoicing())) {
        Beans.get(ProjectTaskRepository.class)
            .all()
            .filter(entity.getExcludeTaskInvoicing())
            .count();
      }
      return super.save(entity);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(
          I18n.get(BusinessProjectExceptionMessage.INVALID_EXCLUDE_TASK_FILTER));
    }
  }
}
