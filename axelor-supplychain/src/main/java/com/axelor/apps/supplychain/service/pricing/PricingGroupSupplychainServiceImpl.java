package com.axelor.apps.supplychain.service.pricing;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.service.pricing.PricingGenericService;
import com.axelor.apps.sale.service.PricingGroupSaleServiceImpl;
import com.axelor.apps.stock.db.StockMove;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class PricingGroupSupplychainServiceImpl extends PricingGroupSaleServiceImpl {

  @Inject
  public PricingGroupSupplychainServiceImpl(PricingGenericService pricingGenericService) {
    super(pricingGenericService);
  }

  @Override
  public String getConcernedModelDomain(Pricing pricing) {
    String domain = super.getConcernedModelDomain(pricing);

    if (PricingRepository.PRICING_TYPE_SELECT_CARRIER_MODE_PRICING.equals(
        pricing.getTypeSelect())) {
      domain =
          String.format(
              "self.name IN (%s)",
              List.of(StockMove.class.getSimpleName()).stream()
                  .map(str -> String.format("'%s'", str))
                  .collect(Collectors.joining(",")));
    }

    return domain;
  }
}
