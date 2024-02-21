package com.axelor.apps.account.service.sublines;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.shiro.util.CollectionUtils;

public class InvoiceSubLineServiceImpl implements InvoiceSubLineService {

  protected final InvoiceLineRepository invoiceLineRepository;
  protected final InvoiceRepository invoiceRepository;
  protected final TaxService taxService;
  protected final AppBaseService appBaseService;
  protected final InvoiceLineService invoiceLineService;
  protected final AppAccountService appAccountService;
  protected final InvoiceService invoiceService;

  @Inject
  public InvoiceSubLineServiceImpl(
      InvoiceLineRepository invoiceLineRepository,
      InvoiceRepository invoiceRepository,
      TaxService taxService,
      AppBaseService appBaseService,
      InvoiceLineService invoiceLineService,
      AppAccountService appAccountService,
      InvoiceService invoiceService) {
    this.invoiceLineRepository = invoiceLineRepository;
    this.invoiceRepository = invoiceRepository;
    this.taxService = taxService;
    this.appBaseService = appBaseService;
    this.invoiceLineService = invoiceLineService;
    this.appAccountService = appAccountService;
    this.invoiceService = invoiceService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateRelatedInvoiceLinesOnPriceChange(InvoiceLine invoiceLine, Invoice invoice)
      throws AxelorException {
    invoiceLine = calculateChildrenTotalsAndPrices(invoiceLine, invoice);
    invoiceLineRepository.save(invoiceLine);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateRelatedInvoiceLinesOnQtyChange(InvoiceLine invoiceLine, Invoice invoice)
      throws AxelorException {
    invoiceLine = updateSubLinesQty(invoiceLine, invoice);
    invoiceLine.setIsProcessedLine(true);
    invoiceLineRepository.save(invoiceLine);
  }

  protected InvoiceLine updateSubLinesQty(InvoiceLine invoiceLine, Invoice invoice)
      throws AxelorException {
    List<InvoiceLine> subLines = invoiceLine.getInvoiceLineList();
    if (subLines == null || subLines.isEmpty()) {
      invoiceLine.setQtyBeforeUpdate(invoiceLine.getQty());
      return invoiceLine;
    }
    BigDecimal qty = invoiceLine.getQty();
    BigDecimal oldQty = invoiceLine.getQtyBeforeUpdate();

    for (InvoiceLine subLine : subLines) {
      subLine.setQty(
          subLine.getQtyBeforeUpdate().multiply(qty).divide(oldQty, 2, RoundingMode.HALF_UP));
      computeAllValues(subLine, invoice);
      updateSubLinesQty(subLine, invoice);
    }

    invoiceLine.setQtyBeforeUpdate(invoiceLine.getQty());

    return invoiceLine;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateRelatedOrderLines(Invoice invoice) throws AxelorException {
    if (!appAccountService.getAppAccount().getIsSubLinesEnabled()) {
      return;
    }

    List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();
    if (CollectionUtils.isEmpty(invoiceLineList)) {
      return;
    }
    for (InvoiceLine invoiceLine : invoiceLineList) {
      calculateAllParentsTotalsAndPrices(invoiceLine, invoice);
    }
    invoice = invoiceService.compute(invoice);
    invoiceRepository.save(invoice);
  }

  protected void calculateAllParentsTotalsAndPrices(InvoiceLine invoiceLine, Invoice invoice)
      throws AxelorException {
    if (invoiceLine.getInvoiceLineList() == null || invoiceLine.getInvoiceLineList().isEmpty()) {
      invoiceLine.setPriceBeforeUpdate(invoiceLine.getPrice());
      invoiceLine.setQtyBeforeUpdate(invoiceLine.getQty());
      setDefaultSaleOrderLineProperties(invoiceLine, invoice);
      return;
    }

    BigDecimal total = computeTotal(invoiceLine, invoice);
    invoiceLine.setPrice(
        total.divide(
            invoiceLine.getQty(),
            appBaseService.getNbDecimalDigitForUnitPrice(),
            RoundingMode.HALF_UP));

    computeAllValues(invoiceLine, invoice);
    invoiceLine.setPriceBeforeUpdate(invoiceLine.getPrice());
    invoiceLine.setQtyBeforeUpdate(invoiceLine.getQty());
    setDefaultSaleOrderLineProperties(invoiceLine, invoice);
  }

  protected BigDecimal computeTotal(InvoiceLine invoiceLine, Invoice invoice)
      throws AxelorException {
    BigDecimal total = BigDecimal.ZERO;
    for (InvoiceLine subline : invoiceLine.getInvoiceLineList()) {
      calculateAllParentsTotalsAndPrices(subline, invoice);
      total = total.add(subline.getExTaxTotal());
    }
    return total;
  }

  protected void setDefaultSaleOrderLineProperties(InvoiceLine invoiceLine, Invoice invoice) {

    InvoiceLine parentSaleOrderLine = invoiceLine.getParentInvoiceLine();
    Integer countType = invoice.getCompany().getAccountConfig().getCountTypeSelect();
    if (parentSaleOrderLine == null && countType == AccountConfigRepository.COUNT_ONLY_PARENTS) {
      invoiceLine.setIsNotCountable(false);
    }
    if (parentSaleOrderLine != null && countType == AccountConfigRepository.COUNT_ONLY_PARENTS) {
      invoiceLine.setIsNotCountable(true);
    }
    if (parentSaleOrderLine == null && countType == AccountConfigRepository.COUNT_ONLY_CHILDREN) {
      invoiceLine.setIsNotCountable(true);
    }
    if (parentSaleOrderLine != null && countType == AccountConfigRepository.COUNT_ONLY_CHILDREN) {
      invoiceLine.setIsNotCountable(false);
    }

    if (parentSaleOrderLine == null
        && CollectionUtils.isEmpty(invoiceLine.getInvoiceLineList())
        && countType == AccountConfigRepository.COUNT_ONLY_CHILDREN) {
      invoiceLine.setIsNotCountable(false);
    }

    if (invoiceLine.getInvoice() == null) {
      invoiceLine.setInvoice(invoice);
    }
    if (invoiceLine.getIsProcessedLine()) {
      invoiceLine.setIsProcessedLine(false);
    }
    if (invoiceLine.getIsDisabledFromCalculation()) {
      invoiceLine.setIsDisabledFromCalculation(false);
    }
  }

  protected InvoiceLine calculateChildrenTotalsAndPrices(InvoiceLine invoiceLine, Invoice invoice)
      throws AxelorException {
    List<InvoiceLine> subLines = invoiceLine.getInvoiceLineList();
    BigDecimal oldQty = invoiceLine.getQty();
    BigDecimal oldPrice = invoiceLine.getPriceBeforeUpdate();
    if (subLines == null || subLines.isEmpty()) {
      invoiceLine.setPriceBeforeUpdate(invoiceLine.getPrice());
      return invoiceLine;
    }

    for (InvoiceLine subLine : subLines) {
      BigDecimal subLineTotal = subLine.getExTaxTotal();
      BigDecimal price =
          invoiceLine
              .getExTaxTotal()
              .multiply(subLineTotal)
              .divide(
                  subLine.getQty().multiply(oldQty.multiply(oldPrice)),
                  AppBaseService.COMPUTATION_SCALING,
                  RoundingMode.HALF_UP);
      subLine.setPrice(
          price.setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP));
      computeAllValues(subLine, invoice);
      calculateChildrenTotalsAndPrices(subLine, invoice);
    }

    invoiceLine.setPriceBeforeUpdate(invoiceLine.getPrice());
    return invoiceLine;
  }

  protected void computeAllValues(InvoiceLine invoiceLine, Invoice invoice) throws AxelorException {
    BigDecimal exTaxPrice = invoiceLine.getPrice();
    Set<TaxLine> taxLineSet = invoiceLine.getTaxLineSet();
    BigDecimal inTaxPrice =
        taxService.convertUnitPrice(
            false, taxLineSet, exTaxPrice, appBaseService.getNbDecimalDigitForUnitPrice());
    invoiceLine.setInTaxPrice(inTaxPrice);
    invoiceLineService.compute(invoice, invoiceLine);
  }

  @Override
  public InvoiceLine setLineIndex(InvoiceLine invoiceLine, Context context) {
    if (!appAccountService.getAppAccount().getIsSubLinesEnabled()) {
      return invoiceLine;
    }

    if (invoiceLine.getLineIndex() == null) {
      Context parentContext = context.getParent();
      if (parentContext != null && parentContext.getContextClass().equals(Invoice.class)) {
        Invoice parent = parentContext.asType(Invoice.class);
        invoiceLine.setLineIndex(calculateParentLineIndex(parent));
      }

      if (context.getParent() != null
          && context.getParent().getContextClass().equals(InvoiceLine.class)) {
        InvoiceLine parent = context.getParent().asType(InvoiceLine.class);
        invoiceLine.setLineIndex(
            parent.getLineIndex() + "." + (parent.getInvoiceLineListSize() + 1));
      }
    }
    return invoiceLine;
  }

  protected String calculateParentLineIndex(Invoice invoice) {
    return invoice.getInvoiceLineList().stream()
        .filter(slo -> slo.getLineIndex() != null)
        .map(slo -> slo.getLineIndex().split("\\.")[0])
        .mapToInt(Integer::parseInt)
        .boxed()
        .collect(Collectors.maxBy(Integer::compareTo))
        .map(max -> String.valueOf(max + 1))
        .orElse("1");
  }

  @Override
  public InvoiceLine updateOnInvoiceLineListChange(InvoiceLine invoiceLine) {
    invoiceLine.setInvoiceLineListSize(invoiceLine.getInvoiceLineList().size());
    for (InvoiceLine line : invoiceLine.getInvoiceLineList()) {
      if (line.getIsProcessedLine() || line.getIsDisabledFromCalculation()) {
        invoiceLine.setIsDisabledFromCalculation(true);
        break;
      }
    }
    return invoiceLine;
  }

  @Override
  public Invoice getParentInvoiceLine(Context context) {
    Context parentContext = context.getParent();

    if (parentContext == null) {
      return null;
    }
    if (parentContext.getContextClass().equals(Invoice.class)) {
      return parentContext.asType(Invoice.class);
    }
    return getParentInvoiceLine(parentContext);
  }

  @Override
  public String getProductDomain(Invoice invoice, boolean isFilterOnSupplier) {
    String domain = null;
    int operationTypeSelect = invoice.getOperationTypeSelect();
    if (operationTypeSelect > 2) {
      domain = "self.isModel = false AND self.dtype = 'Product' AND self.sellable = true";
    }

    if (operationTypeSelect < 3) {
      domain = "self.isModel = false AND self.dtype = 'Product' AND self.purchasable = true";
    }

    return domain;
  }
}
