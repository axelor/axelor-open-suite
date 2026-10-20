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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.interfaces.GlobalDiscounter;
import com.axelor.apps.base.interfaces.GlobalDiscounterLine;
import com.axelor.apps.base.service.discount.GlobalDiscountAbstractService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderGlobalDiscountServiceImpl extends GlobalDiscountAbstractService
    implements SaleOrderGlobalDiscountService {

  protected final SaleOrderComputeService saleOrderComputeService;
  protected final SaleOrderLineComputeService saleOrderLineComputeService;

  @Inject
  public SaleOrderGlobalDiscountServiceImpl(
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderLineComputeService saleOrderLineComputeService) {
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
  }

  @Override
  protected void compute(GlobalDiscounter globalDiscounter) throws AxelorException {
    SaleOrder saleOrder = getSaleOrder(globalDiscounter);
    if (saleOrder == null) {
      return;
    }
    if (CollectionUtils.isNotEmpty(saleOrder.getSaleOrderLineList())) {
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        saleOrderLineComputeService.computeValues(saleOrder, saleOrderLine);
      }
    }
    saleOrderComputeService.computeSaleOrder(saleOrder);
  }

  @Override
  protected List<? extends GlobalDiscounterLine> getGlobalDiscounterLines(
      GlobalDiscounter globalDiscounter) {
    SaleOrder saleOrder = getSaleOrder(globalDiscounter);
    if (saleOrder == null) {
      return new ArrayList<>();
    }
    return saleOrder.getSaleOrderLineList();
  }

  protected SaleOrder getSaleOrder(GlobalDiscounter globalDiscounter) {
    SaleOrder saleOrder = null;
    if (globalDiscounter instanceof SaleOrder) {
      saleOrder = (SaleOrder) globalDiscounter;
    }
    return saleOrder;
  }

  @Override
  public void applyGlobalDiscountOnLines(GlobalDiscounter globalDiscounter) throws AxelorException {
    super.applyGlobalDiscountOnLines(globalDiscounter);
    if (globalDiscounter == null
        || CollectionUtils.isEmpty(getGlobalDiscounterLines(globalDiscounter))
        || globalDiscounter.getDiscountTypeSelect() == PriceListLineRepository.AMOUNT_TYPE_NONE) {
      return;
    }
    compute(globalDiscounter);
  }

  @Override
  public void resetGlobalDiscount(SaleOrder saleOrder) throws AxelorException {
    resetGlobalDiscountOnLines(saleOrder);
    compute(saleOrder);
  }
}
