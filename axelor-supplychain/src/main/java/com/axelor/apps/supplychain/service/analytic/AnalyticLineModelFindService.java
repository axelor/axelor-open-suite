/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.analytic;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.Context;

public class AnalyticLineModelFindService {

  public static AnalyticLineModel getAnalyticLineModel(
      ActionRequest request, AnalyticMoveLine analyticMoveLine) {
    Context parentContext = request.getContext().getParent();

    if (parentContext != null) {
      Class<?> parentClass = parentContext.getContextClass();

      if (SaleOrderLine.class.isAssignableFrom(parentClass)) {
        SaleOrderLine saleOrderLine = parentContext.asType(SaleOrderLine.class);
        SaleOrder saleOrder = getSaleOrderFromContext(saleOrderLine, parentContext);
        return new AnalyticLineModel(saleOrderLine, saleOrder);
      }

      if (PurchaseOrderLine.class.isAssignableFrom(parentClass)) {
        PurchaseOrderLine purchaseOrderLine = parentContext.asType(PurchaseOrderLine.class);
        PurchaseOrder purchaseOrder = getPurchaseOrderFromContext(purchaseOrderLine, parentContext);
        return new AnalyticLineModel(purchaseOrderLine, purchaseOrder);
      }
    }

    if (analyticMoveLine.getSaleOrderLine() != null) {
      SaleOrderLine saleOrderLine =
          Beans.get(SaleOrderLineRepository.class)
              .find(analyticMoveLine.getSaleOrderLine().getId());
      return new AnalyticLineModel(saleOrderLine, saleOrderLine.getSaleOrder());
    }

    if (analyticMoveLine.getPurchaseOrderLine() != null) {
      PurchaseOrderLine purchaseOrderLine =
          Beans.get(PurchaseOrderLineRepository.class)
              .find(analyticMoveLine.getPurchaseOrderLine().getId());
      return new AnalyticLineModel(purchaseOrderLine, purchaseOrderLine.getPurchaseOrder());
    }

    return null;
  }

  protected static SaleOrder getSaleOrderFromContext(
      SaleOrderLine saleOrderLine, Context parentContext) {
    if (saleOrderLine.getSaleOrder() != null) {
      return saleOrderLine.getSaleOrder();
    }

    Context grandParentContext = parentContext.getParent();
    if (grandParentContext != null
        && SaleOrder.class.isAssignableFrom(grandParentContext.getContextClass())) {
      return grandParentContext.asType(SaleOrder.class);
    }

    return null;
  }

  protected static PurchaseOrder getPurchaseOrderFromContext(
      PurchaseOrderLine purchaseOrderLine, Context parentContext) {
    if (purchaseOrderLine.getPurchaseOrder() != null) {
      return purchaseOrderLine.getPurchaseOrder();
    }

    Context grandParentContext = parentContext.getParent();
    if (grandParentContext != null
        && PurchaseOrder.class.isAssignableFrom(grandParentContext.getContextClass())) {
      return grandParentContext.asType(PurchaseOrder.class);
    }

    return null;
  }
}
