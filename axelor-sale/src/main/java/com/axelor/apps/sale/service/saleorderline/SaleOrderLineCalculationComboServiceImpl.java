/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorderline.saleorderlinetree.SaleOrderLineTreeComputationService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Map;

public class SaleOrderLineCalculationComboServiceImpl
    implements SaleOrderLineCalculationComboService {

  protected SaleOrderLineTreeComputationService saleOrderLineTreeCalculationsService;
  protected SaleOrderLineComputeService saleOrderLineComputeService;

  @Inject
  public SaleOrderLineCalculationComboServiceImpl(
      SaleOrderLineTreeComputationService saleOrderLineTreeCalculationsService,
      SaleOrderLineComputeService saleOrderLineComputeService) {
    this.saleOrderLineTreeCalculationsService = saleOrderLineTreeCalculationsService;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Map<String, BigDecimal> computePriceAndRelatedFields(SaleOrderLine saleOrderLine)
      throws AxelorException {

    saleOrderLineTreeCalculationsService.computePrices(saleOrderLine);

    saleOrderLineComputeService.computeValues(saleOrderLine.getSaleOrder(), saleOrderLine);
    saleOrderLineTreeCalculationsService.computeSubTotalCostPrice(saleOrderLine);
    return Beans.get(SaleOrderMarginService.class)
        .getSaleOrderLineComputedMarginInfo(saleOrderLine.getSaleOrder(), saleOrderLine);
  }
}
