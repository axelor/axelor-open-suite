package com.axelor.apps.sale.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.pricing.PricingGenericServiceImpl;
import com.axelor.apps.base.service.pricing.PricingObserver;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.google.inject.Inject;

public class SalePricingGenericServiceImpl extends PricingGenericServiceImpl {

  @Inject
  public SalePricingGenericServiceImpl(
      PricingService pricingService, PricingObserver pricingObserver) {
    super(pricingService, pricingObserver);
  }

  @Override
  public void computePricingsOnChildren(Company company, Model model) throws AxelorException {
    super.computePricingsOnChildren(company, model);

    if (SaleOrder.class.equals(EntityHelper.getEntityClass(model))) {
      SaleOrder saleOrder = (SaleOrder) model;
      if (!ObjectUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
        for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
          computePricingsOnModel(company, saleOrderLine);
        }
      }
    }
  }
}
