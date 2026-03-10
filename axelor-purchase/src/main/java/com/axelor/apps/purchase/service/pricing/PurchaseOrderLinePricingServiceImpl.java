/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.service.pricing.PricingGenericService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseOrderLinePricingServiceImpl implements PurchaseOrderLinePricingService {

  protected PricingGenericService pricingGenericService;

  @Inject
  public PurchaseOrderLinePricingServiceImpl(PricingGenericService pricingGenericService) {
    this.pricingGenericService = pricingGenericService;
  }

  @Override
  public void computePricingScale(PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder)
      throws AxelorException {
    Map<String, Object> contextMap = new HashMap<>();
    contextMap.put("purchaseOrder", purchaseOrder);

    List<StringBuilder> logsList =
        pricingGenericService.computePricingProcess(
            purchaseOrder.getCompany(),
            purchaseOrderLine,
            PricingRepository.PRICING_TYPE_SELECT_PURCHASE_PRICING,
            contextMap);
  }
}
