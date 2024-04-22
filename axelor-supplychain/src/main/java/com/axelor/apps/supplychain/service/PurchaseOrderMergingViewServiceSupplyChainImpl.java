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

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.PurchaseOrderMergingService;
import com.axelor.apps.purchase.service.PurchaseOrderMergingService.PurchaseOrderMergingResult;
import com.axelor.apps.purchase.service.PurchaseOrderMergingViewServiceImpl;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;
import java.util.List;

public class PurchaseOrderMergingViewServiceSupplyChainImpl
    extends PurchaseOrderMergingViewServiceImpl {

  protected AppPurchaseService appPurchaseService;
  protected PurchaseOrderMergingServiceSupplyChainImpl purchaseOrderMergingSupplyChainService;

  @Inject
  public PurchaseOrderMergingViewServiceSupplyChainImpl(
      PurchaseOrderMergingService purchaseOrderMergingService,
      AppPurchaseService appPurchaseService,
      PurchaseOrderMergingServiceSupplyChainImpl purchaseOrderMergingSupplyChainService) {
    super(purchaseOrderMergingService);
    this.appPurchaseService = appPurchaseService;
    this.purchaseOrderMergingSupplyChainService = purchaseOrderMergingSupplyChainService;
  }

  @Override
  public ActionViewBuilder buildConfirmView(
      PurchaseOrderMergingResult result, List<PurchaseOrder> purchaseOrdersToMerge) {

    ActionViewBuilder confirmView = super.buildConfirmView(result, purchaseOrdersToMerge);
    if (purchaseOrderMergingSupplyChainService.getChecks(result).isExistStockLocationDiff()) {
      confirmView.context("contextLocationToCheck", Boolean.TRUE.toString());
    }
    return confirmView;
  }
}
