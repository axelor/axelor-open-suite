/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bpm.db.repo;

import com.axelor.apps.bpm.db.WkfModel;

public class BpmWkfModelRepository extends WkfModelRepository {

  @Override
  public WkfModel copy(WkfModel entity, boolean deep) {

    WkfModel copyModel = super.copy(entity, deep);
    copyModel.setWkfProcessList(null);
    copyModel.setWkfTaskConfigList(null);
    copyModel.setStatusSelect(WkfModelRepository.STATUS_NEW);

    return copyModel;
  }

  @Override
  public void remove(WkfModel entity) {

    if (entity.getPreviousVersion() != null) {
      remove(entity.getPreviousVersion());
    }

    super.remove(entity);
  }
}
