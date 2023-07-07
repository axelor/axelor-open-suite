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
