package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.Company;

public class SaleOrderDomainServiceImpl implements SaleOrderDomainService {
  @Override
  public String getPartnerBaseDomain(Company company) {
    Long companyPartnerId = company.getPartner() == null ? 0 : company.getPartner().getId();
    return String.format(
        "self.id != %d AND self.isContact = false "
            + "AND (self.isCustomer = true or self.isProspect = true) "
            + "AND :company member of self.companySet",
        companyPartnerId);
  }
}
