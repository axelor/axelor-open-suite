package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineGeneratorSupplyChain;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InvoicingProjectInvoiceServiceImpl implements InvoicingProjectInvoiceService {

  @Override
  public List<InvoiceLine> createCusInvFromSupInvLines(
      Invoice invoice, Set<InvoiceLine> invMoveLineSet, int priority) throws AxelorException {
    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 1;
    for (InvoiceLine invoiceLine : invMoveLineSet) {

      invoiceLineList.addAll(this.createInvoiceLine(invoice, invoiceLine, priority * 100 + count));
      count++;
    }

    return invoiceLineList;
  }

  protected List<InvoiceLine> createInvoiceLine(
      Invoice invoice, InvoiceLine invoiceLine, int sequence) throws AxelorException {
    Product product = invoiceLine.getProduct();

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGeneratorSupplyChain(
            invoice,
            product,
            invoiceLine.getProductName(),
            invoiceLine.getDescription(),
            invoiceLine.getQty(),
            invoiceLine.getUnit(),
            sequence,
            false,
            null,
            null,
            null,
            invoiceLine) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();
            invoiceLine.setProject(invoiceLine.getProject());
            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }
}
