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
package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderOnLineChangeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePackService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductService;
import com.google.inject.Inject;

public class SaleOrderOnLineChangeSupplyChainServiceImpl extends SaleOrderOnLineChangeServiceImpl {

  protected SaleOrderSupplychainService saleOrderSupplychainService;
  protected SaleOrderShipmentService saleOrderShipmentService;

  @Inject
  public SaleOrderOnLineChangeSupplyChainServiceImpl(
      AppSaleService appSaleService,
      SaleOrderService saleOrderService,
      SaleOrderMarginService saleOrderMarginService,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLinePackService saleOrderLinePackService,
      SaleOrderLineProductService saleOrderLineProductService,
      SaleOrderSupplychainService saleOrderSupplychainService,
      SaleOrderShipmentService saleOrderShipmentService) {
    super(
        appSaleService,
        saleOrderService,
        saleOrderMarginService,
        saleOrderComputeService,
        saleOrderLineRepository,
        saleOrderLineComputeService,
        saleOrderLinePackService,
        saleOrderLineProductService);
    this.saleOrderSupplychainService = saleOrderSupplychainService;
    this.saleOrderShipmentService = saleOrderShipmentService;
  }

  @Override
  public void onLineChange(SaleOrder saleOrder) throws AxelorException {
    super.onLineChange(saleOrder);
    saleOrderSupplychainService.setAdvancePayment(saleOrder);
    saleOrderSupplychainService.updateTimetableAmounts(saleOrder);
    saleOrderSupplychainService.updateAmountToBeSpreadOverTheTimetable(saleOrder);
    saleOrderShipmentService.createShipmentCostLine(saleOrder);
  }
}
