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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproject.service.SaleOrderLineInitValueProjectServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineAnalyticService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChain;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineInitValueBusinessProductionServiceImpl
    extends SaleOrderLineInitValueProjectServiceImpl {

  protected final SaleOrderLineProductionService saleOrderLineProductionService;

  @Inject
  public SaleOrderLineInitValueBusinessProductionServiceImpl(
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      AppSupplychainService appSupplychainService,
      SaleOrderLineAnalyticService saleOrderLineAnalyticService,
      SaleOrderLineProductionService saleOrderLineProductionService) {
    super(saleOrderLineServiceSupplyChain, appSupplychainService, saleOrderLineAnalyticService);
    this.saleOrderLineProductionService = saleOrderLineProductionService;
  }

  @Override
  public Map<String, Object> onNewInitValues(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, SaleOrderLine parentSol)
      throws AxelorException {
    Map<String, Object> values = super.onNewInitValues(saleOrder, saleOrderLine, parentSol);
    values.putAll(fillQtyToProduce(saleOrderLine, parentSol));
    return values;
  }

  @Override
  public Map<String, Object> onNewEditableInitValues(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, SaleOrderLine parentSol) {
    Map<String, Object> values = super.onNewEditableInitValues(saleOrder, saleOrderLine, parentSol);
    values.putAll(fillQtyToProduce(saleOrderLine, parentSol));
    return values;
  }

  protected Map<String, Object> fillQtyToProduce(
      SaleOrderLine saleOrderLine, SaleOrderLine parentSol) {
    Map<String, Object> values = new HashMap<>();

    saleOrderLine.setQtyToProduce(
        saleOrderLineProductionService.computeQtyToProduce(saleOrderLine, parentSol));
    values.put("qtyToProduce", saleOrderLine.getQtyToProduce());
    return values;
  }
}
