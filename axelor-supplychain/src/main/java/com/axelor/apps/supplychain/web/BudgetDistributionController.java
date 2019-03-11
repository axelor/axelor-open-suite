/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.BudgetLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.supplychain.service.BudgetSupplychainService;
import com.axelor.apps.supplychain.service.InvoiceLineSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplychainImpl;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class BudgetDistributionController {

  public void changebudgetLineDomain(ActionRequest request, ActionResponse response) {
    Context parentContext = request.getContext().getParent();
    List<BudgetLine> budgetLineList = null;

    if (parentContext.get("_model").equals("com.axelor.apps.account.db.InvoiceLine")) {
      Invoice invoice = parentContext.getParent().asType(Invoice.class);
      budgetLineList =
          Beans.get(InvoiceLineSupplychainService.class).changebudgetLineDomain(invoice);
    } else if (parentContext
        .get("_model")
        .equals("com.axelor.apps.purchase.db.PurchaseOrderLine")) {
      PurchaseOrder purchaseOrder = parentContext.getParent().asType(PurchaseOrder.class);
      budgetLineList =
          Beans.get(PurchaseOrderLineServiceSupplychainImpl.class)
              .changebudgetLineDomain(purchaseOrder);
    }

    String domain =
        Beans.get(BudgetSupplychainService.class).changebudgetLineDomain(budgetLineList);

    response.setAttr("budgetLine", "domain", domain);
  }
}
