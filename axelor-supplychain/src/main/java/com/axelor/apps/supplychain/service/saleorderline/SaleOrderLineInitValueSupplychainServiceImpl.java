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
package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineInitValueServiceImpl;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.studio.db.AppSupplychain;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineInitValueSupplychainServiceImpl
    extends SaleOrderLineInitValueServiceImpl {

  protected SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain;
  protected AppSupplychainService appSupplychainService;
  protected SaleOrderLineAnalyticService saleOrderLineAnalyticService;

  @Inject
  public SaleOrderLineInitValueSupplychainServiceImpl(
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      AppSupplychainService appSupplychainService,
      SaleOrderLineAnalyticService saleOrderLineAnalyticService) {
    this.saleOrderLineServiceSupplyChain = saleOrderLineServiceSupplyChain;
    this.appSupplychainService = appSupplychainService;
    this.saleOrderLineAnalyticService = saleOrderLineAnalyticService;
  }

  @Override
  public Map<String, Object> onNewInitValues(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, SaleOrderLine parentSol)
      throws AxelorException {
    Map<String, Object> values = super.onNewInitValues(saleOrder, saleOrderLine, parentSol);
    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();
    if (appSupplychain.getManageStockReservation()) {
      values.putAll(saleOrderLineServiceSupplyChain.updateRequestedReservedQty(saleOrderLine));
    }
    values.putAll(fillRequestQty(saleOrder, saleOrderLine));
    values.putAll(saleOrderLineAnalyticService.printAnalyticAccounts(saleOrder, saleOrderLine));
    return values;
  }

  @Override
  public Map<String, Object> onLoadInitValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> values = super.onLoadInitValues(saleOrder, saleOrderLine);
    values.putAll(saleOrderLineAnalyticService.printAnalyticAccounts(saleOrder, saleOrderLine));
    return values;
  }

  @Override
  public Map<String, Object> onNewEditableInitValues(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, SaleOrderLine parentSol) {
    Map<String, Object> values = super.onNewEditableInitValues(saleOrder, saleOrderLine, parentSol);
    values.putAll(fillRequestQty(saleOrder, saleOrderLine));
    return values;
  }

  protected Map<String, Object> fillRequestQty(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> values = new HashMap<>();
    SupplyChainConfig supplyChainConfig = saleOrder.getCompany().getSupplyChainConfig();
    if (supplyChainConfig == null) {
      return values;
    }
    boolean autoRequestReservedQty = supplyChainConfig.getAutoRequestReservedQty();
    saleOrderLine.setIsQtyRequested(autoRequestReservedQty);
    if (autoRequestReservedQty) {
      saleOrderLine.setRequestedReservedQty(saleOrderLine.getQty());
    }

    values.put("isQtyRequested", saleOrderLine.getIsQtyRequested());
    values.put("requestedReservedQty", saleOrderLine.getRequestedReservedQty());

    return values;
  }
}
