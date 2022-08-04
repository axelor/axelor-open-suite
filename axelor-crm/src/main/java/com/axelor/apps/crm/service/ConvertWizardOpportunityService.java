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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.wizard.ConvertWizardService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.Map;

public class ConvertWizardOpportunityService {

  protected ConvertWizardService convertWizardService;
  protected OpportunityService opportunityService;

  @Inject
  public ConvertWizardOpportunityService(
      ConvertWizardService convertWizardService, OpportunityService opportunityService) {
    this.convertWizardService = convertWizardService;
    this.opportunityService = opportunityService;
  }

  public void createOpportunity(Map<String, Object> context, Partner partner)
      throws AxelorException {
    Opportunity opportunity =
        (Opportunity)
            convertWizardService.createObject(
                context, Mapper.toBean(Opportunity.class, null), Mapper.of(Opportunity.class));
    opportunity.setPartner(partner);
    opportunityService.saveOpportunity(opportunity);
  }
}
