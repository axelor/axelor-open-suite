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
package com.axelor.apps.budget.model;

import com.axelor.apps.businessproject.model.AnalyticLineProjectModel;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;

public class AnalyticLineBudgetModel extends AnalyticLineProjectModel {

  public AnalyticLineBudgetModel(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    super(saleOrderLine, saleOrder);

    this.account = saleOrderLine.getAccount();
  }

  public AnalyticLineBudgetModel(PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) {
    super(purchaseOrderLine, purchaseOrder);

    this.account = purchaseOrderLine.getAccount();
  }
}
