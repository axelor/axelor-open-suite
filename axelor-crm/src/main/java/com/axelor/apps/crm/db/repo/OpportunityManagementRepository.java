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

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.OpportunityStatus;
import com.axelor.apps.crm.service.OpportunityService;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppCrm;
import java.util.Map;
import javax.persistence.PersistenceException;

public class OpportunityManagementRepository extends OpportunityRepository {

  @Override
  public Opportunity copy(Opportunity entity, boolean deep) {
    Opportunity copy = super.copy(entity, deep);
    OpportunityStatus status = Beans.get(OpportunityStatusRepository.class).getDefaultStatus();
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

      return super.save(opportunity);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      final String closedWonId = "$closedWonId";
      final String closedLostId = "$closedLostId";

      AppCrm appCrm = Beans.get(AppCrmService.class).getAppCrm();

      if (appCrm.getClosedWinOpportunityStatus() != null) {
        json.put(closedWonId, appCrm.getClosedWinOpportunityStatus().getId());
      }

      if (appCrm.getClosedLostOpportunityStatus() != null) {
        json.put(closedLostId, appCrm.getClosedLostOpportunityStatus().getId());
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return super.populate(json, context);
  }
}
