package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Company;

public class PurchaseOrderDomainServiceImpl implements PurchaseOrderDomainService {
  @Override
  public String getPartnerBaseDomain(Company company) {
    long companyId = company.getPartner() == null ? 0L : company.getPartner().getId();
    return String.format(
        "self.id != %d AND self.isContact = false AND self.isSupplier = true AND :company member of self.companySet",
        companyId);
  }
}
