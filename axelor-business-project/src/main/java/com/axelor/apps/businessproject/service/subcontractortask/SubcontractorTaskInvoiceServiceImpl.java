package com.axelor.apps.businessproject.service.subcontractortask;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.businessproject.db.SubcontractorTask;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SubcontractorTaskInvoiceServiceImpl implements SubcontractorTaskInvoiceService {

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<SubcontractorTask> subcontractorTaskList, int priority)
      throws AxelorException {
    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;

    for (SubcontractorTask subcontractorTask : subcontractorTaskList) {
      invoiceLineList.addAll(
          this.createInvoiceLine(invoice, subcontractorTask, priority * 100 + count));
      count++;
    }

    return invoiceLineList;
  }

  public List<InvoiceLine> createInvoiceLine(
      Invoice invoice, SubcontractorTask subcontractorTask, int priority) throws AxelorException {
    Product product = subcontractorTask.getProduct();

    String productName = product.getName();
    BigDecimal quantity = subcontractorTask.getTimeSpent();
    BigDecimal unitPrice = product.getSalePrice();
    BigDecimal totalAmount = unitPrice.multiply(quantity);

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            product,
            productName,
            unitPrice,
            unitPrice,
            unitPrice,
            subcontractorTask.getComments(),
            quantity,
            product.getUnit(),
            null,
            priority,
            BigDecimal.ZERO,
            PriceListLineRepository.AMOUNT_TYPE_NONE,
            totalAmount,
            totalAmount,
            false) {

          public List<InvoiceLine> creates() throws AxelorException {
            InvoiceLine invoiceLine = this.createInvoiceLine();
            invoiceLine.setProject(subcontractorTask.getProject());

            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);
            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }
}
