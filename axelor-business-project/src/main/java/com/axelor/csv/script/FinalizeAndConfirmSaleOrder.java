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
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderConfirmService;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderFinalizeService;
import com.google.inject.Inject;
import java.util.Map;

public class FinalizeAndConfirmSaleOrder {

  protected final SaleOrderConfirmService saleOrderConfirmService;
  protected final SaleOrderFinalizeService saleOrderFinalizeService;

  @Inject
  public FinalizeAndConfirmSaleOrder(
      SaleOrderConfirmService saleOrderConfirmService,
      SaleOrderFinalizeService saleOrderFinalizeService) {
    this.saleOrderConfirmService = saleOrderConfirmService;
    this.saleOrderFinalizeService = saleOrderFinalizeService;
  }

  public Object finalizeAndConfirmSaleOrder(Object bean, Map<String, Object> values)
      throws AxelorException {
    assert bean instanceof SaleOrder;

    SaleOrder saleOrder = (SaleOrder) bean;

    if (saleOrder.getStatusSelect().equals(SaleOrderRepository.STATUS_ORDER_CONFIRMED)) {
      saleOrder.setStatusSelect(SaleOrderRepository.STATUS_DRAFT_QUOTATION);
      saleOrderFinalizeService.finalizeQuotation(saleOrder);
      saleOrderConfirmService.confirmSaleOrder(saleOrder);
    }
    return saleOrder;
  }
}
