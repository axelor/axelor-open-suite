package com.axelor.apps.sale.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.service.pricing.PricingGenericServiceImpl;
import com.axelor.apps.base.service.pricing.PricingObserver;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SalePricingGenericServiceImpl extends PricingGenericServiceImpl {

  protected AppSaleService appSaleService;

  @Inject
  public SalePricingGenericServiceImpl(
      PricingService pricingService,
      PricingObserver pricingObserver,
      AppSaleService appSaleService) {
    super(pricingService, pricingObserver);
    this.appSaleService = appSaleService;
  }

  @Override
  public List<Pricing> getPricings(Company company, Model model) {
    List<Pricing> pricingList = new ArrayList<>();
    if (appSaleService.getAppSale().getIsPricingComputingOrder()) {
      pricingList =
          pricingService.getPricings(company, model, null).stream().collect(Collectors.toList());
    } else {
      List<Pricing> resultList = pricingService.getAllPricings(company, model);

      Set<Long> pricingsPointedTo =
          resultList.stream()
              .map(Pricing::getLinkedPricing)
              .filter(Objects::nonNull)
              .map(Pricing::getId)
              .collect(Collectors.toSet());

      // find the pricing that doesn't have any pricing pointing to it, that's the root
      for (Pricing pricing : resultList) {
        if (!pricingsPointedTo.contains(pricing.getId())) {
          pricingList.add(pricing);
        }
      }
    }

    return pricingList;
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

  @Override
  public String updatePricingScaleLogs(List<StringBuilder> logsList, Model model) {
    String logs = super.updatePricingScaleLogs(logsList, model);

    if (SaleOrderLine.class.equals(EntityHelper.getEntityClass(model))) {
      SaleOrderLine saleOrderLine = (SaleOrderLine) model;
      saleOrderLine.setPricingScaleLogs(logs);
    }

    return logs;
  }
}
