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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineAnalyticService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineInitValueSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChain;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineInitValueProjectServiceImpl
    extends SaleOrderLineInitValueSupplychainServiceImpl {

  @Inject
  public SaleOrderLineInitValueProjectServiceImpl(
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      AppSupplychainService appSupplychainService,
      SaleOrderLineAnalyticService saleOrderLineAnalyticService) {
    super(saleOrderLineServiceSupplyChain, appSupplychainService, saleOrderLineAnalyticService);
  }

  @Override
  public Map<String, Object> onNewInitValues(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, SaleOrderLine parentSol)
      throws AxelorException {
    Map<String, Object> values = super.onNewInitValues(saleOrder, saleOrderLine, parentSol);
    values.putAll(fillProject(saleOrder, saleOrderLine));
    return values;
  }

  @Override
  public Map<String, Object> onNewEditableInitValues(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, SaleOrderLine parentSol) {
    Map<String, Object> values = super.onNewEditableInitValues(saleOrder, saleOrderLine, parentSol);
    values.putAll(fillProject(saleOrder, saleOrderLine));
    return values;
  }

  protected Map<String, Object> fillProject(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> values = new HashMap<>();
    Project project = saleOrder.getProject();
    saleOrderLine.setProject(project);
    values.put("project", project);
    return values;
  }
}
