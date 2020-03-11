/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.service.MrpService;
import com.axelor.inject.Beans;

public class MrpManagementRepository extends MrpRepository {

  @Override
  public void remove(Mrp entity) {

    Beans.get(MrpService.class).reset(entity);

    super.save(entity);
  }

  @Override
  public Mrp copy(Mrp entity, boolean deep) {
    Mrp copy = super.copy(entity, deep);
    copy.setStatusSelect(MrpManagementRepository.STATUS_DRAFT);
    copy.setStartDateTime(null);
    copy.setEndDateTime(null);
    return copy;
  }
}
