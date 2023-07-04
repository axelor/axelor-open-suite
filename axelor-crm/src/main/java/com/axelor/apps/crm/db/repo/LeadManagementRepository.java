/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class LeadManagementRepository extends LeadRepository {

  protected AppCrmService appCrmService;

  @Inject
  public LeadManagementRepository(AppCrmService appCrmService) {
    this.appCrmService = appCrmService;
  }

  @Override
  public Lead copy(Lead entity, boolean deep) {
    Lead lead = super.copy(entity, deep);
    lead.setContactDate(null);
    if (appCrmService.getAppCrm() != null
        && appCrmService.getAppCrm().getLeadDefaultStatus() != null) {
      lead.setLeadStatus(appCrmService.getAppCrm().getLeadDefaultStatus());
    } else {
      try {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(CrmExceptionMessage.CRM_DEFAULT_LEAD_STATUS_MISSING));
      } catch (AxelorException e) {
        throw new PersistenceException(e);
      }
    }
    return lead;
  }

  @Override
  public Lead save(Lead entity) {
    try {
      LeadService leadService = Beans.get(LeadService.class);

      String fullName =
          leadService.processFullName(
              entity.getEnterpriseName(), entity.getName(), entity.getFirstName());
      entity.setFullName(fullName);

      if (entity.getLeadStatus() == null) {
        entity.setLeadStatus(leadService.getDefaultLeadStatus());
      }

      return super.save(entity);

    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
