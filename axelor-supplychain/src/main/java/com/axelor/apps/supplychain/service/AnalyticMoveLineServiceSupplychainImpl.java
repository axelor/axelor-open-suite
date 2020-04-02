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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.AnalyticMoveLineServiceImpl;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.rpc.Context;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class AnalyticMoveLineServiceSupplychainImpl extends AnalyticMoveLineServiceImpl {

  public AnalyticMoveLineServiceSupplychainImpl(
      AppAccountService appAccountService, AccountManagementService accountManagementService) {
    super(appAccountService, accountManagementService);
  }

  @Override
  public BigDecimal computeAmount(AnalyticMoveLine analyticMoveLine) {
    BigDecimal amount = BigDecimal.ZERO;
    if (analyticMoveLine.getPurchaseOrderLine() != null) {
      amount =
          analyticMoveLine
              .getPercentage()
              .multiply(
                  analyticMoveLine
                      .getPurchaseOrderLine()
                      .getExTaxTotal()
                      .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
    }
    if (analyticMoveLine.getSaleOrderLine() != null) {
      amount =
          analyticMoveLine
              .getPercentage()
              .multiply(
                  analyticMoveLine
                      .getSaleOrderLine()
                      .getExTaxTotal()
                      .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
    }

    if (amount.compareTo(BigDecimal.ZERO) == 0) {
      return super.computeAmount(analyticMoveLine);
    }
    return amount;
  }

  @Override
  public BigDecimal chooseComputeWay(Context context, AnalyticMoveLine analyticMoveLine) {
    if (analyticMoveLine.getPurchaseOrderLine() == null
        && analyticMoveLine.getSaleOrderLine() == null) {
      if (context.getParent().getContextClass() == PurchaseOrderLine.class) {
        analyticMoveLine.setPurchaseOrderLine(context.getParent().asType(PurchaseOrderLine.class));
      } else if (context.getParent().getContextClass() == InvoiceLine.class) {
        analyticMoveLine.setInvoiceLine(context.getParent().asType(InvoiceLine.class));
      } else if (context.getParent().getContextClass() == MoveLine.class) {
        analyticMoveLine.setMoveLine(context.getParent().asType(MoveLine.class));
      } else {
        analyticMoveLine.setSaleOrderLine(context.getParent().asType(SaleOrderLine.class));
      }
    }
    return this.computeAmount(analyticMoveLine);
  }
}
