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
package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.db.WorkCenterType;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.i18n.I18n;
import javax.persistence.PersistenceException;

public class WorkCenterTypeProductionRepository extends WorkCenterTypeRepository {

  @Override
  public WorkCenterType save(WorkCenterType entity) {
    WorkCenterType workCenterType = super.save(entity);
    if (workCenterType
            .getWorkCenterList()
            .stream()
            .filter(
                workCenterList ->
                    workCenterList.getTypeSelect() == WorkCenterRepository.DEFAULT_WORK_CENTER)
            .count()
        > 1) {
      throw new PersistenceException(I18n.get(IExceptionMessage.WORK_CENTER_TYPE_SELECT_ERROR));
    }
    return workCenterType;
  }
}
