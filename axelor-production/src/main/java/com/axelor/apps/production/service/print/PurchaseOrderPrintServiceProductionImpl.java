/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service.print;

import com.axelor.apps.production.module.ProductionModule;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.purchase.service.print.PurchaseOrderPrintServiceImpl;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

@Alternative
@Priority(ProductionModule.PRIORITY)
public class PurchaseOrderPrintServiceProductionImpl extends PurchaseOrderPrintServiceImpl {

  @Inject
  public PurchaseOrderPrintServiceProductionImpl(AppPurchaseService appPurchaseService) {
    super(appPurchaseService);
  }

  @Override
  public String getPurchaseOrderLineQuerySelectClause() {

    return super.getPurchaseOrderLineQuerySelectClause()
        .concat(", Product.product_standard as product_standard");
  }
}
