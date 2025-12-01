package com.axelor.apps.businessproject.service.extraexpense;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.businessproject.db.ExtraExpenseLine;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExtraExpenseInvoiceServiceImpl implements ExtraExpenseInvoiceService {
  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<ExtraExpenseLine> extraExpenseLineList, int priority)
      throws AxelorException {
    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;

    for (ExtraExpenseLine extraExpenseLine : extraExpenseLineList) {
      invoiceLineList.addAll(
          this.createInvoiceLine(invoice, extraExpenseLine, priority * 100 + count));
      count++;
      extraExpenseLine.setInvoiced(true);
    }

    return invoiceLineList;
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(
      Invoice invoice, ExtraExpenseLine extraExpenseLine, int priority) throws AxelorException {

    Product product = extraExpenseLine.getExpenseProduct();

    // Format date for product name if present
    String productName = product.getName();
    if (extraExpenseLine.getExpenseDate() != null) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
      productName += " (" + extraExpenseLine.getExpenseDate().format(formatter) + ")";
    }

    BigDecimal unitPrice = extraExpenseLine.getPrice();
    BigDecimal quantity = extraExpenseLine.getQuantity();
    BigDecimal totalAmount = extraExpenseLine.getTotalAmount();

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            product,
            productName,
            unitPrice,
            unitPrice,
            unitPrice,
            extraExpenseLine.getComments(),
            quantity,
            product.getUnit(),
            null,
            priority,
            BigDecimal.ZERO,
            PriceListLineRepository.AMOUNT_TYPE_NONE,
            totalAmount,
            totalAmount,
            false) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {
            InvoiceLine invoiceLine = this.createInvoiceLine();
            invoiceLine.setProject(extraExpenseLine.getProject());

            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);
            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }
}
