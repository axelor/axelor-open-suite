package com.axelor.apps.contract.service.pricing;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.service.pricing.PricingGenericService;
import com.axelor.apps.sale.service.PricingGroupSaleServiceImpl;
import com.google.inject.Inject;

public class PricingGroupContractServiceImpl extends PricingGroupSaleServiceImpl {

  @Inject
  public PricingGroupContractServiceImpl(PricingGenericService pricingGenericService) {
    super(pricingGenericService);
  }

  @Override
  public String getConcernedModelDomain(Pricing pricing) {
    String domain = super.getConcernedModelDomain(pricing);

    if (PricingRepository.PRICING_TYPE_SELECT_CONTRACT_YEB_YER.equals(pricing.getTypeSelect())) {
      domain = String.format("self.name = '%s'", InvoiceLine.class.getSimpleName());
    }

    return domain;
  }
}
