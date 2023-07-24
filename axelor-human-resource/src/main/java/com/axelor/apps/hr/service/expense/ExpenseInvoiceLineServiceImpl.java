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
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.hr.db.ExpenseLine;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ExpenseInvoiceLineServiceImpl implements ExpenseInvoiceLineService {

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<ExpenseLine> expenseLineList, int priority) throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    for (ExpenseLine expenseLine : expenseLineList) {

      invoiceLineList.addAll(this.createInvoiceLine(invoice, expenseLine, priority * 100 + count));
      count++;
      expenseLine.setInvoiced(true);
    }

    return invoiceLineList;
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(Invoice invoice, ExpenseLine expenseLine, int priority)
      throws AxelorException {

    Product product = expenseLine.getExpenseProduct();
    InvoiceLineGenerator invoiceLineGenerator = null;
    Integer atiChoice = invoice.getCompany().getAccountConfig().getInvoiceInAtiSelect();
    if (atiChoice == AccountConfigRepository.INVOICE_WT_ALWAYS
        || atiChoice == AccountConfigRepository.INVOICE_WT_DEFAULT) {
      invoiceLineGenerator =
          new InvoiceLineGenerator(
              invoice,
              product,
              product.getName(),
              expenseLine.getUntaxedAmount(),
              expenseLine.getTotalAmount(),
              expenseLine.getUntaxedAmount(),
              expenseLine.getComments(),
              BigDecimal.ONE,
              product.getUnit(),
              null,
              priority,
              BigDecimal.ZERO,
              PriceListLineRepository.AMOUNT_TYPE_NONE,
              expenseLine.getUntaxedAmount(),
              expenseLine.getTotalAmount(),
              false) {

            @Override
            public List<InvoiceLine> creates() throws AxelorException {

              InvoiceLine invoiceLine = this.createInvoiceLine();

              List<InvoiceLine> invoiceLines = new ArrayList<>();
              invoiceLines.add(invoiceLine);

              return invoiceLines;
            }
          };
    } else {
      invoiceLineGenerator =
          new InvoiceLineGenerator(
              invoice,
              product,
              product.getName(),
              expenseLine.getUntaxedAmount(),
              expenseLine.getTotalAmount(),
              expenseLine.getTotalAmount(),
              expenseLine.getComments(),
              BigDecimal.ONE,
              product.getUnit(),
              null,
              priority,
              BigDecimal.ZERO,
              PriceListLineRepository.AMOUNT_TYPE_NONE,
              expenseLine.getUntaxedAmount(),
              expenseLine.getTotalAmount(),
              false) {

            @Override
            public List<InvoiceLine> creates() throws AxelorException {

              InvoiceLine invoiceLine = this.createInvoiceLine();

              List<InvoiceLine> invoiceLines = new ArrayList<>();
              invoiceLines.add(invoiceLine);

              return invoiceLines;
            }
          };
    }

    return invoiceLineGenerator.creates();
  }
}
