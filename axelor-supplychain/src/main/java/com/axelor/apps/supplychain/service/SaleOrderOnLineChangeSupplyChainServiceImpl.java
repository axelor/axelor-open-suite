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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderOnLineChangeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.google.inject.Inject;

public class SaleOrderOnLineChangeSupplyChainServiceImpl extends SaleOrderOnLineChangeServiceImpl {

  protected SaleOrderSupplychainService saleOrderSupplychainService;

  @Inject
  public SaleOrderOnLineChangeSupplyChainServiceImpl(
      AppSaleService appSaleService,
      SaleOrderService saleOrderService,
      SaleOrderLineService saleOrderLineService,
      SaleOrderMarginService saleOrderMarginService,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderSupplychainService saleOrderSupplychainService) {
    super(
        appSaleService,
        saleOrderService,
        saleOrderLineService,
        saleOrderMarginService,
        saleOrderComputeService,
        saleOrderLineRepository);
    this.saleOrderSupplychainService = saleOrderSupplychainService;
  }

  @Override
  public void onLineChange(SaleOrder saleOrder) throws AxelorException {
    super.onLineChange(saleOrder);
    saleOrderSupplychainService.setAdvancePayment(saleOrder);
    saleOrderSupplychainService.updateTimetableAmounts(saleOrder);
    saleOrderSupplychainService.updateAmountToBeSpreadOverTheTimetable(saleOrder);
  }
}
