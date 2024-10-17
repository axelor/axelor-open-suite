package com.axelor.apps.businessproject.service;

import com.axelor.apps.sale.db.SaleOrder;

public class SaleOrderLineDomainProjectServiceImpl implements SaleOrderLineDomainProjectService {

  @Override
  public String getProjectDomain(SaleOrder saleOrder) {
    StringBuilder domain = new StringBuilder();
    domain.append("self.clientPartner.id = ");
    domain.append(saleOrder.getClientPartner().getId());
    domain.append(" AND self.isBusinessProject = TRUE");

    return domain.toString();
  }
}
