package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class ConvertWizardOpportunityServiceImpl implements ConvertWizardOpportunityService {

  protected OpportunityService opportunityService;

  @Inject
  public ConvertWizardOpportunityServiceImpl(OpportunityService opportunityService) {
    super();
    this.opportunityService = opportunityService;
  }

  @Override
  public void createOpportunity(Opportunity opportunity, Partner partner) throws AxelorException {
    opportunity.setPartner(partner);
    opportunityService.saveOpportunity(opportunity);
  }
}
