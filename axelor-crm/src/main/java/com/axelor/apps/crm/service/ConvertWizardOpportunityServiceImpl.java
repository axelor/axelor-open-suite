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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.wizard.ConvertWizardService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.db.mapper.Mapper;
import com.google.inject.Inject;
import java.util.Map;

public class ConvertWizardOpportunityServiceImpl implements ConvertWizardOpportunityService {

  protected OpportunityService opportunityService;
  protected ConvertWizardService convertWizardService;

  @Inject
  public ConvertWizardOpportunityServiceImpl(
      OpportunityService opportunityService, ConvertWizardService convertWizardService) {
    this.opportunityService = opportunityService;
    this.convertWizardService = convertWizardService;
  }

  @Override
  public void createOpportunity(Map<String, Object> opportunityMap, Partner partner)
      throws AxelorException {
    Opportunity opportunity =
        (Opportunity)
            convertWizardService.createObject(
                opportunityMap,
                Mapper.toBean(Opportunity.class, null),
                Mapper.of(Opportunity.class));
    opportunity.setPartner(partner);
    opportunityService.saveOpportunity(opportunity);
  }
}
