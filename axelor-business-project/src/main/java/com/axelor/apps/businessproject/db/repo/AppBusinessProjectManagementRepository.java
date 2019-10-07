/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.base.db.AppBusinessProject;
import com.axelor.apps.base.db.repo.AppBusinessProjectRepository;
import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class AppBusinessProjectManagementRepository extends AppBusinessProjectRepository {

  @Inject private TeamTaskRepository teamTaskRepo;

  @Override
  public AppBusinessProject save(AppBusinessProject entity) {
    try {
      if (!Strings.isNullOrEmpty(entity.getExculdeTaskInvoicing())) {
        teamTaskRepo.all().filter(entity.getExculdeTaskInvoicing()).count();
      }
      return super.save(entity);
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new PersistenceException(I18n.get(IExceptionMessage.INVALID_EXCLUDE_TASK_FILTER));
    }
  }
}
