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
import com.axelor.apps.crm.service.OpportunitySequenceService;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.studio.db.AppCrm;
import com.google.inject.Inject;
import java.util.Map;
import javax.persistence.PersistenceException;

public class OpportunityManagementRepository extends OpportunityRepository {

  protected AppCrmService appCrmService;
  protected OpportunitySequenceService opportunitySequenceService;

  @Inject
  public OpportunityManagementRepository(
      AppCrmService appCrmService, OpportunitySequenceService opportunitySequenceService) {
    this.appCrmService = appCrmService;
    this.opportunitySequenceService = opportunitySequenceService;
  }

  @Override
  public Opportunity copy(Opportunity entity, boolean deep) {
    Opportunity copy = super.copy(entity, deep);
    try {
      OpportunityStatus status = appCrmService.getOpportunityDefaultStatus();
      copy.setOpportunityStatus(status);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
    copy.setLostReason(null);
    copy.setOpportunitySeq(null);
    copy.setExpectedCloseDate(null);
    return copy;
  }

  @Override
  public Opportunity save(Opportunity opportunity) {
    try {
      if (opportunity.getOpportunitySeq() == null) {
        opportunitySequenceService.setSequence(opportunity);
      }

      if (opportunity.getOpportunityStatus() == null) {
        opportunity.setOpportunityStatus(appCrmService.getOpportunityDefaultStatus());
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

      AppCrm appCrm = appCrmService.getAppCrm();

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
