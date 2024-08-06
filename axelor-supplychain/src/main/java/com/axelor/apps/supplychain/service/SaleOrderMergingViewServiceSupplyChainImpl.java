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

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService.SaleOrderMergingResult;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingViewServiceImpl;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;
import java.util.List;

public class SaleOrderMergingViewServiceSupplyChainImpl extends SaleOrderMergingViewServiceImpl {

  protected AppSaleService appSaleService;
  protected SaleOrderMergingServiceSupplyChainImpl saleOrderMergingSupplyChainService;

  @Inject
  public SaleOrderMergingViewServiceSupplyChainImpl(
      SaleOrderMergingService saleOrderMergingService,
      AppSaleService appSaleService,
      SaleOrderMergingServiceSupplyChainImpl saleOrderMergingSupplyChainService) {
    super(saleOrderMergingService);
    this.appSaleService = appSaleService;
    this.saleOrderMergingSupplyChainService = saleOrderMergingSupplyChainService;
  }

  @Override
  public ActionViewBuilder buildConfirmView(
      SaleOrderMergingResult result, String lineToMerge, List<SaleOrder> saleOrderToMerge) {
    if (!appSaleService.isApp("supplychain")) {
      return super.buildConfirmView(result, lineToMerge, saleOrderToMerge);
    }

    ActionViewBuilder confirmView = super.buildConfirmView(result, lineToMerge, saleOrderToMerge);
    if (saleOrderMergingSupplyChainService.getChecks(result).isExistStockLocationDiff()) {
      confirmView.context("contextLocationToCheck", Boolean.TRUE.toString());
    }
    return confirmView;
  }
}
