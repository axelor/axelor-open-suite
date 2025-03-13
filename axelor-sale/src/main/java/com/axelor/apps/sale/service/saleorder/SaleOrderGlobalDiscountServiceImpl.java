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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.interfaces.GlobalDiscounter;
import com.axelor.apps.base.interfaces.GlobalDiscounterLine;
import com.axelor.apps.base.service.discount.GlobalDiscountAbstractService;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class SaleOrderGlobalDiscountServiceImpl extends GlobalDiscountAbstractService
    implements SaleOrderGlobalDiscountService {

  protected final SaleOrderComputeService saleOrderComputeService;

  @Inject
  public SaleOrderGlobalDiscountServiceImpl(SaleOrderComputeService saleOrderComputeService) {
    this.saleOrderComputeService = saleOrderComputeService;
  }

  @Override
  protected void compute(GlobalDiscounter globalDiscounter) throws AxelorException {
    SaleOrder saleOrder = getSaleOrder(globalDiscounter);
    if (saleOrder == null) {
      return;
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
}
