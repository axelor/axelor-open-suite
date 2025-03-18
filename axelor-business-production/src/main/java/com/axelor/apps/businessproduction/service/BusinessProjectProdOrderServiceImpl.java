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
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.studio.db.AppProduction;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BusinessProjectProdOrderServiceImpl implements BusinessProjectProdOrderService {

  protected final ProductionOrderSaleOrderService productionOrderSaleOrderService;
  protected final AppProductionService appProductionService;

  @Inject
  public BusinessProjectProdOrderServiceImpl(
      ProductionOrderSaleOrderService productionOrderSaleOrderService,
      AppProductionService appProductionService) {
    this.productionOrderSaleOrderService = productionOrderSaleOrderService;
    this.appProductionService = appProductionService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public List<ProductionOrder> generateProductionOrders(Project project) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = project.getSaleOrderLineList();
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return Collections.emptyList();
    }

    List<ProductionOrder> productionOrderList = new ArrayList<>();
    AppProduction appProduction = appProductionService.getAppProduction();
    boolean oneProdOrderPerSo = appProduction.getOneProdOrderPerSO();

    if (oneProdOrderPerSo) {
      generateOnePoPerSo(saleOrderLineList, productionOrderList);
    } else {
      generateOnePoPerSol(saleOrderLineList, productionOrderList);
    }

    return productionOrderList;
  }

  protected void generateOnePoPerSo(
      List<SaleOrderLine> saleOrderLineList, List<ProductionOrder> productionOrderList)
      throws AxelorException {
    Set<SaleOrder> saleOrderSet =
        saleOrderLineList.stream().map(SaleOrderLine::getSaleOrder).collect(Collectors.toSet());
    for (SaleOrder saleOrder : saleOrderSet) {
      List<SaleOrderLine> filteredSaleOrderLineList =
          saleOrderLineList.stream()
              .filter(line -> line.getSaleOrder().equals(saleOrder))
              .collect(Collectors.toList());
      ProductionOrder productionOrder =
          productionOrderSaleOrderService.createProductionOrder(saleOrder);
      for (SaleOrderLine saleOrderLine : filteredSaleOrderLineList) {
        productionOrderSaleOrderService.generateManufOrders(productionOrder, saleOrderLine);
      }
      productionOrderList.add(productionOrder);
    }
  }

  protected void generateOnePoPerSol(
      List<SaleOrderLine> saleOrderLineList, List<ProductionOrder> productionOrderList)
      throws AxelorException {
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      SaleOrder saleOrder = saleOrderLine.getSaleOrder();
      ProductionOrder productionOrder =
          productionOrderSaleOrderService.createProductionOrder(saleOrder);
      productionOrderSaleOrderService.generateManufOrders(productionOrder, saleOrderLine);
      productionOrderList.add(productionOrder);
    }
  }
}
