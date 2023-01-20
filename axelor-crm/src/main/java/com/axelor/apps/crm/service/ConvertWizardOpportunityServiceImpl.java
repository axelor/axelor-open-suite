package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.wizard.ConvertWizardService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.Map;

public class ConvertWizardOpportunityServiceImpl implements ConvertWizardOpportunityService {

  protected OpportunityService opportunityService;
  protected ConvertWizardService convertWizardService;

  @Inject
  public ConvertWizardOpportunityServiceImpl(
      OpportunityService opportunityService, ConvertWizardService convertWizardService) {
    super();
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
