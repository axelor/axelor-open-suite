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
package com.axelor.apps.crm.db.repo;

import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.service.OpportunityService;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class OpportunityManagementRepository extends OpportunityRepository {
  @Override
  public Opportunity copy(Opportunity entity, boolean deep) {
    Opportunity copy = super.copy(entity, deep);
    copy.setSalesStageSelect(OpportunityRepository.SALES_STAGE_NEW);
    copy.setLostReason(null);
    return copy;
  }

  @Override
  public Opportunity save(Opportunity opportunity) {
    try {
      if (opportunity.getOpportunitySeq() == null) {
        Beans.get(OpportunityService.class).setSequence(opportunity);
      }
      return super.save(opportunity);
    } catch (Exception e) {
      throw new PersistenceException(e);
    }
  }
}
