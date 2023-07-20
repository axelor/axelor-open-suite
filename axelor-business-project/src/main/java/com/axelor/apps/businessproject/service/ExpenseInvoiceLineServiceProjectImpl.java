/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.service.expense.ExpenseInvoiceLineServiceImpl;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ExpenseInvoiceLineServiceProjectImpl extends ExpenseInvoiceLineServiceImpl {

  protected AppAccountService appAccountService;

  @Inject
  public ExpenseInvoiceLineServiceProjectImpl(AppAccountService appAccountService) {
    this.appAccountService = appAccountService;
  }

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<ExpenseLine> expenseLineList, int priority) throws AxelorException {

    if (!appAccountService.isApp("business-project")) {
      return super.createInvoiceLines(invoice, expenseLineList, priority);
    }

    List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
    int count = 0;
    for (ExpenseLine expenseLine : expenseLineList) {

      invoiceLineList.addAll(this.createInvoiceLine(invoice, expenseLine, priority * 100 + count));
      count++;
      invoiceLineList.get(invoiceLineList.size() - 1).setProject(expenseLine.getProject());
    }

    return invoiceLineList;
  }
}
