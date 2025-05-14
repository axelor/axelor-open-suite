package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.inject.Beans;

public class SaleOrderDateServiceImpl implements SaleOrderDateService {

  @Override
  public SaleOrder computeEndOfValidityDate(SaleOrder saleOrder) {
    Company company = saleOrder.getCompany();
    if (saleOrder.getDuration() == null && company != null && company.getSaleConfig() != null) {
      saleOrder.setDuration(company.getSaleConfig().getDefaultValidityDuration());
    }
    if (saleOrder.getCreationDate() != null) {
      saleOrder.setEndOfValidityDate(
          Beans.get(DurationService.class)
              .computeDuration(saleOrder.getDuration(), saleOrder.getCreationDate()));
    }
    return saleOrder;
  }
}
