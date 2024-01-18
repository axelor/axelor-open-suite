package com.axelor.apps.sale.service;

import com.axelor.apps.base.service.pricing.PricingMetaServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;

public class SalePricingMetaServiceImpl extends PricingMetaServiceImpl {

  @Override
  public String setButtonCondition(String model) {
    String condition = super.setButtonCondition(model);

    if (model != null && SaleOrder.class.getName().equals(model)) {
      condition =
          condition.concat(
              " && __config__.app.getApp('sale')?.getIsEnableCalculationEntireQuotationUsingPricing()");
    }

    return condition;
  }
}
