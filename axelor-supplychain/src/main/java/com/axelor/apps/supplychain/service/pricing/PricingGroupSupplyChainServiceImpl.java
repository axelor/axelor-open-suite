/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service.pricing;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.service.pricing.PricingGenericService;
import com.axelor.apps.sale.service.PricingGroupSaleServiceImpl;
import com.axelor.apps.supplychain.db.FreightCarrierPricing;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class PricingGroupSupplyChainServiceImpl extends PricingGroupSaleServiceImpl {

  @Inject
  public PricingGroupSupplyChainServiceImpl(PricingGenericService pricingGenericService) {
    super(pricingGenericService);
  }

  @Override
  public String getConcernedModelDomain(Pricing pricing) {
    String domain = super.getConcernedModelDomain(pricing);

    if (PricingRepository.PRICING_TYPE_SELECT_FREIGHT_CARRIER_PRICING.equals(
            pricing.getTypeSelect())
        || PricingRepository.PRICING_TYPE_SELECT_FREIGHT_CARRIER_DELAY.equals(
            pricing.getTypeSelect())) {
      domain =
          String.format(
              "self.name IN (%s)",
              List.of(FreightCarrierPricing.class.getSimpleName()).stream()
                  .map(str -> String.format("'%s'", str))
                  .collect(Collectors.joining(",")));
    }

    return domain;
  }
}
