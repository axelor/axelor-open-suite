package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;

public class PartnerEmailDomainToolServiceImpl implements PartnerEmailDomainToolService {

  protected EmailDomainToolService<Partner> emailDomainToolService;

  @Inject
  public PartnerEmailDomainToolServiceImpl(EmailDomainToolService<Partner> emailDomainToolService) {
    this.emailDomainToolService = emailDomainToolService;
  }

  @Override
  public String computeFilterEmailOnDomain(Partner partner) {
    return emailDomainToolService.computeFilterEmailOnDomain(
        partner.getEmailAddress(), "self.isContact = true");
  }
}
