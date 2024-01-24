package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.project.db.HoldBackLine;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HoldBackLineServiceImpl implements HoldBackLineService {

  public Invoice generateInvoiceLinesForHoldBacks(Invoice invoice) throws AxelorException {
    List<HoldBackLine> holdBackLineList = invoice.getProject().getHoldBackLineList();
    if (holdBackLineList == null || holdBackLineList.isEmpty()) {
      return invoice;
    }

    List<InvoiceLine> invoiceLineList = createInvoiceLines(invoice, holdBackLineList, invoice.getInvoiceLineList().size());
    invoice.getInvoiceLineList().addAll(invoiceLineList);
    return invoice;
  }

  protected List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<HoldBackLine> holdBackLineList, int priority) throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    for (HoldBackLine holdBackLine : holdBackLineList) {
      invoiceLineList.addAll(this.createInvoiceLine(invoice, holdBackLine, priority * 100 + count));
      count++;
    }
    return invoiceLineList;
  }

  protected List<InvoiceLine> createInvoiceLine(
      Invoice invoice, HoldBackLine holdBackLine, int priority) throws AxelorException {

    BigDecimal price = calculateHoldBackLinePrice(invoice, holdBackLine);

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            holdBackLine.getHoldBack().getHoldBackProduct(),
            holdBackLine.getHoldBack().getName(),
            price,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            null,
            BigDecimal.ONE,
            null,
            null,
            priority,
            BigDecimal.ZERO,
            0,
            price,
            BigDecimal.ZERO,
            false) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();

            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }

  BigDecimal calculateHoldBackLinePrice(Invoice invoice, HoldBackLine holdBackLine) {
    BigDecimal price;
    BigDecimal percentage = holdBackLine.getPercentage();
    Set<Product> products = holdBackLine.getHoldBack().getProductsHeldBackSet();
    List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();
    if (products == null || products.isEmpty()) {
      price =
          invoiceLineList.stream()
              .map(InvoiceLine::getPrice)
              .reduce(BigDecimal.ZERO, BigDecimal::add)
              .multiply(percentage.divide(BigDecimal.valueOf(100)));
    } else {
      price =
          invoiceLineList.stream()
              .filter(invLine -> products.contains(invLine.getProduct()))
              .map(InvoiceLine::getPrice)
              .reduce(BigDecimal.ZERO, BigDecimal::add)
              .multiply(percentage.divide(BigDecimal.valueOf(100)));
    }

    return price.negate();
  }
}
