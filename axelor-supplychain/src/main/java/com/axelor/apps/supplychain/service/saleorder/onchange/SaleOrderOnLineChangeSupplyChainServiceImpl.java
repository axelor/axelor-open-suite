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
package com.axelor.apps.supplychain.service.saleorder.onchange;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComplementaryProductService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderGlobalDiscountService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorder.onchange.SaleOrderOnLineChangeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderShipmentService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderSupplychainService;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
      SaleOrderComplementaryProductService saleOrderComplementaryProductService,
      SaleOrderGlobalDiscountService saleOrderGlobalDiscountService,
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
        saleOrderComplementaryProductService,
        saleOrderGlobalDiscountService);
    this.saleOrderSupplychainService = saleOrderSupplychainService;
    this.saleOrderShipmentService = saleOrderShipmentService;
  }

  @Override
  public String onLineChange(SaleOrder saleOrder) throws AxelorException {
    super.onLineChange(saleOrder);
    List<String> messages = new ArrayList<>();
    saleOrderSupplychainService.setAdvancePayment(saleOrder);
    messages.add(saleOrderSupplychainService.updateTimetableAmounts(saleOrder));
    saleOrderSupplychainService.updateAmountToBeSpreadOverTheTimetable(saleOrder);
    messages.add(
        saleOrderShipmentService.createShipmentCostLine(saleOrder, saleOrder.getShipmentMode()));
    return messages.stream()
        .filter(Objects::nonNull)
        .filter(Predicate.not(String::isEmpty))
        .collect(Collectors.joining("<br>"));
  }
}
