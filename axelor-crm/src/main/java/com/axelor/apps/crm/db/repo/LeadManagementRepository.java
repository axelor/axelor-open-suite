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
package com.axelor.apps.crm.db.repo;

import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.inject.Beans;

public class LeadManagementRepository extends LeadRepository {

  @Override
  public Lead save(Lead entity) {
    LeadService leadService = Beans.get(LeadService.class);

    String fullName =
        leadService.processFullName(
            entity.getEnterpriseName(), entity.getName(), entity.getFirstName());
    entity.setFullName(fullName);

    if (entity.getLeadStatus() == null) {
      entity.setLeadStatus(leadService.getDefaultLeadStatus());
    }

    return super.save(entity);
  }
}
