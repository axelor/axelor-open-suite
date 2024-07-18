package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrder;

public class SaleOrderLineDomainSupplychainServiceImpl
    implements SaleOrderLineDomainSupplychainService {
  @Override
  public String getAnalyticDistributionTemplateDomain(SaleOrder saleOrder) {
    StringBuilder domain = new StringBuilder();
    domain.append("self.company.id = ");
    domain.append(saleOrder.getCompany().getId());
    return domain.toString();
  }
}
