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

import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.inject.Beans;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class LeadManagementRepository extends LeadRepository {

  @Override
  public Lead save(Lead entity) {
    String fullName =
        Beans.get(LeadService.class)
            .processFullName(entity.getEnterpriseName(), entity.getName(), entity.getFirstName());
    entity.setFullName(fullName);

    List<Opportunity> opportunities = entity.getOpportunitiesList();

    if (CollectionUtils.isNotEmpty(opportunities) && entity.getStatusSelect() == LEAD_STATUS_NEW) {
      entity.setStatusSelect(LEAD_STATUS_IN_PROCESS);

    } else if (CollectionUtils.isEmpty(opportunities)
        && entity.getStatusSelect() == LEAD_STATUS_IN_PROCESS) {
      entity.setStatusSelect(LEAD_STATUS_NEW);
    }

    if (entity.getStatusSelect() == LEAD_STATUS_CLOSED
        && entity.getClosedReason() == CLOSED_REASON_CANCELED
        && CollectionUtils.isNotEmpty(opportunities)) {
      for (Opportunity opportunity : opportunities) {
        opportunity.setSalesStageSelect(OpportunityRepository.SALES_STAGE_CLOSED_LOST);
      }
    }

    return super.save(entity);
  }
}
