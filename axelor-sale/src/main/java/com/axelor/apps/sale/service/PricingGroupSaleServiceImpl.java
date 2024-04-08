package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.service.pricing.PricingGenericService;
import com.axelor.apps.base.service.pricing.PricingGroupServiceImpl;
import com.google.inject.Inject;

public class PricingGroupSaleServiceImpl extends PricingGroupServiceImpl {

  @Inject
  public PricingGroupSaleServiceImpl(PricingGenericService pricingGenericService) {
    super(pricingGenericService);
  }

  @Override
  public String getConcernedModelDomain(Pricing pricing) {
    String domain = super.getConcernedModelDomain(pricing);

    if (PricingRepository.PRICING_TYPE_SELECT_SALE_PRICING.equals(pricing.getTypeSelect())) {
      domain =
          domain.concat(String.format(" AND self.packageName = '%s'", "com.axelor.apps.sale.db"));
    }

    return domain;
  }
}
