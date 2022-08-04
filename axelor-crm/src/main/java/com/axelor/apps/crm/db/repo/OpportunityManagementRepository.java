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
package com.axelor.apps.crm.db.repo;

import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.OpportunityStatus;
import com.axelor.apps.crm.service.OpportunityService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class OpportunityManagementRepository extends OpportunityRepository {
  @Override
  public Opportunity copy(Opportunity entity, boolean deep) {
    Opportunity copy = super.copy(entity, deep);
    OpportunityStatus status =
        Beans.get(OpportunityStatusRepository.class)
            .findByTypeSelect(OpportunityStatusRepository.STATUS_TYPE_EMPTY);
    copy.setOpportunityStatus(status);
    copy.setLostReason(null);
    copy.setOpportunitySeq(null);
    return copy;
  }

  @Override
  public Opportunity save(Opportunity opportunity) {
    try {
      OpportunityService opportunityService = Beans.get(OpportunityService.class);
      if (opportunity.getOpportunitySeq() == null) {
        opportunityService.setSequence(opportunity);
      }

      if (opportunity.getOpportunityStatus() == null) {
        opportunity.setOpportunityStatus(opportunityService.getDefaultOpportunityStatus());
      }
      // will be added later
      // opportunity.setName(Beans.get(OpportunityService.class).computeAndGetName(opportunity));

      return super.save(opportunity);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
