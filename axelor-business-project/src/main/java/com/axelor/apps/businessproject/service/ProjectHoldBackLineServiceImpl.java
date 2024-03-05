package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.project.db.ProjectHoldBackLine;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProjectHoldBackLineServiceImpl implements ProjectHoldBackLineService {

  @Override
  public Invoice generateInvoiceLinesForHoldBacks(Invoice invoice) throws AxelorException {
    List<ProjectHoldBackLine> projectHoldBackLineList =
        invoice.getProject().getProjectHoldBackLineList();
    if (projectHoldBackLineList == null || projectHoldBackLineList.isEmpty()) {
      return invoice;
    }

    List<InvoiceLine> invoiceLineList =
        createInvoiceLines(invoice, projectHoldBackLineList, invoice.getInvoiceLineList().size());
    invoice.getInvoiceLineList().addAll(invoiceLineList);
    return invoice;
  }

  protected List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<ProjectHoldBackLine> projectHoldBackLineList, int priority)
      throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    for (ProjectHoldBackLine projectHoldBackLine : projectHoldBackLineList) {
      invoiceLineList.addAll(
          this.createInvoiceLine(invoice, projectHoldBackLine, priority * 100 + count));
      count++;
    }
    return invoiceLineList;
  }

  protected List<InvoiceLine> createInvoiceLine(
      Invoice invoice, ProjectHoldBackLine projectHoldBackLine, int priority)
      throws AxelorException {

    BigDecimal price = calculateHoldBackLinePrice(invoice, projectHoldBackLine);

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            projectHoldBackLine.getProjectHoldBack().getProjectHoldBackProduct(),
            projectHoldBackLine.getProjectHoldBack().getName(),
            price,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            null,
            BigDecimal.ONE,
            projectHoldBackLine.getProjectHoldBack().getProjectHoldBackProduct().getUnit(),
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

  protected BigDecimal calculateHoldBackLinePrice(
      Invoice invoice, ProjectHoldBackLine projectHoldBackLine) {
    BigDecimal price;
    BigDecimal percentage = projectHoldBackLine.getPercentage();
    Set<Product> products = projectHoldBackLine.getProjectHoldBack().getProductsHeldBackSet();
    List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();
    if (products == null || products.isEmpty()) {
      price =
          invoiceLineList.stream()
              .map(InvoiceLine::getExTaxTotal)
              .reduce(BigDecimal.ZERO, BigDecimal::add)
              .multiply(percentage.divide(BigDecimal.valueOf(100)));
    } else {
      price =
          invoiceLineList.stream()
              .filter(invLine -> products.contains(invLine.getProduct()))
              .map(InvoiceLine::getExTaxTotal)
              .reduce(BigDecimal.ZERO, BigDecimal::add)
              .multiply(percentage.divide(BigDecimal.valueOf(100)));
    }

    return price.negate();
  }
}
