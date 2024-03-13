package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.project.db.ProjectHoldBackLine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.shiro.util.CollectionUtils;

public class ProjectHoldBackLineServiceImpl implements ProjectHoldBackLineService {

  protected final AppAccountService appAccountService;
  protected final InvoiceLineService invoiceLineService;

  @Inject
  public ProjectHoldBackLineServiceImpl(
      AppAccountService appAccountService, InvoiceLineService invoiceLineService) {
    this.appAccountService = appAccountService;
    this.invoiceLineService = invoiceLineService;
  }

  @Override
  public Invoice generateInvoiceLinesForHoldBacks(Invoice invoice) throws AxelorException {
    List<ProjectHoldBackLine> projectHoldBackLineList =
        invoice.getProject().getProjectHoldBackLineList();
    if (projectHoldBackLineList == null || projectHoldBackLineList.isEmpty()) {
      return invoice;
    }
    Boolean isSubLinesEnabled = appAccountService.getAppAccount().getIsSubLinesEnabled();
    if (!isSubLinesEnabled && CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
      return invoice;
    }
    if (isSubLinesEnabled && CollectionUtils.isEmpty(invoice.getInvoiceLineDisplayList())) {
      return invoice;
    }

    List<InvoiceLine> invoiceLineList =
        createInvoiceLines(invoice, projectHoldBackLineList, invoice.getInvoiceLineList().size());
    invoice.getInvoiceLineList().addAll(invoiceLineList);

    if (isSubLinesEnabled) {
      for (InvoiceLine invoiceLine : invoiceLineList) {
        invoiceLineService.compute(invoice, invoiceLine);
        invoice.addInvoiceLineDisplayListItem(invoiceLine);
      }
    }

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
            price,
            projectHoldBackLine.getProjectHoldBack().getProjectHoldBackProduct().getDescription(),
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
            invoiceLine.setLineIndex("RT");
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
    List<InvoiceLine> invoiceLineList;
    if (appAccountService.getAppAccount().getIsSubLinesEnabled()) {
      invoiceLineList =
          invoice.getInvoiceLineDisplayList().stream()
              .filter(invoiceLine -> !invoiceLine.getIsNotCountable())
              .collect(Collectors.toList());
    } else {
      invoiceLineList = invoice.getInvoiceLineList();
    }
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
