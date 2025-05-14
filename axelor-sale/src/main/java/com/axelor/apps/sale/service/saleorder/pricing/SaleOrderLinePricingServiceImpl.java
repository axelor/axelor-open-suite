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
package com.axelor.apps.sale.service.saleorder.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.pricing.PricingComputer;
import com.axelor.apps.base.service.pricing.PricingGenericService;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleOrderLinePricingServiceImpl implements SaleOrderLinePricingService {

  protected AppBaseService appBaseService;
  protected PricingService pricingService;
  protected PricingGenericService pricingGenericService;

  @Inject
  public SaleOrderLinePricingServiceImpl(
      AppBaseService appBaseService,
      PricingService pricingService,
      PricingGenericService pricingGenericService) {
    this.appBaseService = appBaseService;
    this.pricingService = pricingService;
    this.pricingGenericService = pricingGenericService;
  }

  @Override
  public void computePricingScale(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> contextMap = new HashMap<>();
    contextMap.put("saleOrder", saleOrder);

    List<StringBuilder> logsList =
        pricingGenericService.computePricingProcess(
            saleOrder.getCompany(),
            saleOrderLine,
            PricingRepository.PRICING_TYPE_SELECT_SALE_PRICING,
            contextMap);
  }

  @Override
  public boolean hasPricingLine(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {

    List<Pricing> pricingList =
        pricingGenericService.getPricings(
            saleOrder.getCompany(),
            saleOrderLine,
            PricingRepository.PRICING_TYPE_SELECT_SALE_PRICING);

    if (!ObjectUtils.isEmpty(pricingList)) {
      for (Pricing pricing : pricingList) {
        if (!PricingComputer.of(pricing, saleOrderLine)
            .putInContext("saleOrder", EntityHelper.getEntity(saleOrder))
            .getMatchedPricingLines()
            .isEmpty()) {
          return true;
        }
      }
    }

    return false;
  }
}
