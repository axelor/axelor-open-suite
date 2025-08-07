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
package com.axelor.apps.businessproject.service.analytic;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.common.ObjectUtils;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ProjectAnalyticMoveLineServiceImpl implements ProjectAnalyticMoveLineService {

  @Override
  public PurchaseOrder updateLines(PurchaseOrder purchaseOrder) {
    if (ObjectUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      return purchaseOrder;
    }

    for (PurchaseOrderLine orderLine : purchaseOrder.getPurchaseOrderLineList()) {
      orderLine.setProject(purchaseOrder.getProject());
      List<AnalyticMoveLine> analyticMoveLineList = orderLine.getAnalyticMoveLineList();
      if (ObjectUtils.notEmpty(analyticMoveLineList)) {
        for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
          analyticMoveLine.setProject(purchaseOrder.getProject());
        }
      }
    }
    return purchaseOrder;
  }

  @Override
  public SaleOrder updateLines(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return saleOrder;
    }
    for (SaleOrderLine orderLine : saleOrderLineList) {
      orderLine.setProject(saleOrder.getProject());
      List<AnalyticMoveLine> analyticMoveLines = orderLine.getAnalyticMoveLineList();
      if (ObjectUtils.notEmpty(analyticMoveLines)) {
        for (AnalyticMoveLine analyticMoveLine : analyticMoveLines) {
          analyticMoveLine.setProject(saleOrder.getProject());
        }
      }
    }
    return saleOrder;
  }
}
