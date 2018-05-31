/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.db.repo;

import com.axelor.apps.crm.db.Lead;

public class LeadManagementRepository extends LeadRepository {

  @Override
  public Lead save(Lead entity) {

    if (entity.getUser() != null && entity.getStatusSelect() == LEAD_STATUS_NEW) {
      entity.setStatusSelect(LEAD_STATUS_ASSIGNED);
    } else if (entity.getUser() == null && entity.getStatusSelect() == LEAD_STATUS_ASSIGNED) {
      entity.setStatusSelect(LEAD_STATUS_NEW);
    }
    return super.save(entity);
  }
}
