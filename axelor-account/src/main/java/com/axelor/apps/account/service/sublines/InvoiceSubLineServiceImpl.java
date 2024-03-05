package com.axelor.apps.account.service.sublines;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.CurrencyScaleServiceAccount;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.generator.tax.TaxInvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
  public void populateInvoiceLines(Invoice invoice) throws AxelorException {

    if (!appAccountService.getAppAccount().getIsSubLinesEnabled()) {
      return;
    }
    updateRelatedOrderLines(invoice);

    invoice = invoiceRepository.find(invoice.getId());
    invoice.getInvoiceLineList().clear();
    for (InvoiceLine invoiceLine : invoice.getInvoiceLineDisplayList()) {
      if (!invoiceLine.getIsNotCountable()) {
        invoice.addInvoiceLineListItem(invoiceLine);
      }
      // invoiceRepository.save(invoice);
    }
    computeInvoice(invoice);
    invoiceRepository.save(invoice);
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
  public void updateRelatedOrderLines(Invoice invoice) throws AxelorException {
    if (!appAccountService.getAppAccount().getIsSubLinesEnabled()) {
      return;
    }
    List<InvoiceLine> invoiceLineDisplayListList = invoice.getInvoiceLineDisplayList();
    if (CollectionUtils.isEmpty(invoiceLineDisplayListList)) {
      return;
    }
    for (InvoiceLine invoiceLine : invoiceLineDisplayListList) {
      calculateAllParentsTotalsAndPrices(invoiceLine, invoice);
      // invoiceLineRepository.save(invoiceLine);
    }
    // invoiceRepository.save(invoice);
  }

  protected void calculateAllParentsTotalsAndPrices(InvoiceLine invoiceLine, Invoice invoice)
      throws AxelorException {
    if (invoiceLine.getInvoiceLineList() == null || invoiceLine.getInvoiceLineList().isEmpty()) {
      invoiceLine.setPriceBeforeUpdate(invoiceLine.getPrice());
      invoiceLine.setQtyBeforeUpdate(invoiceLine.getQty());
      setDefaultInvoiceLineProperties(invoiceLine, invoice);
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
    setDefaultInvoiceLineProperties(invoiceLine, invoice);
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

  protected void setDefaultInvoiceLineProperties(InvoiceLine invoiceLine, Invoice invoice) {

    InvoiceLine parentInvoiceLine = invoiceLine.getParentInvoiceLine();
    Integer countType = invoice.getCompany().getAccountConfig().getCountTypeSelect();
    List<InvoiceLine> invoiceLineList = invoiceLine.getInvoiceLineList();

    if (invoiceLine.getInvoiceDisplay() == null) {
      invoiceLine.setInvoiceDisplay(invoice);
    }
    if (parentInvoiceLine == null && countType == AccountConfigRepository.COUNT_ONLY_PARENTS) {
      invoiceLine.setIsNotCountable(false);
    }
    if (parentInvoiceLine != null && countType == AccountConfigRepository.COUNT_ONLY_PARENTS) {
      invoiceLine.setIsNotCountable(true);
    }
    if (parentInvoiceLine == null && countType == AccountConfigRepository.COUNT_ONLY_CHILDREN) {
      invoiceLine.setIsNotCountable(true);
    }
    if (parentInvoiceLine != null
        && CollectionUtils.isEmpty(invoiceLineList)
        && countType == AccountConfigRepository.COUNT_ONLY_CHILDREN) {
      invoiceLine.setIsNotCountable(false);
    }

    if (parentInvoiceLine == null
        && CollectionUtils.isEmpty(invoiceLine.getInvoiceLineList())
        && countType == AccountConfigRepository.COUNT_ONLY_CHILDREN) {
      invoiceLine.setIsNotCountable(false);
    }
    if (parentInvoiceLine != null
        && !CollectionUtils.isEmpty(invoiceLineList)
        && countType == AccountConfigRepository.COUNT_ONLY_CHILDREN) {
      invoiceLine.setIsNotCountable(true);
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
        if (parent.getInvoiceLineDisplayList() != null) {
          invoiceLine.setLineIndex(calculateParentLineIndex(parent));
        } else {
          invoiceLine.setLineIndex("1");
        }
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
    return invoice.getInvoiceLineDisplayList().stream()
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

  public void computeInvoice(Invoice invoice) throws AxelorException {

    CurrencyScaleServiceAccount currencyScaleServiceAccount =
        Beans.get(CurrencyScaleServiceAccount.class);

    // In the invoice currency
    invoice.setExTaxTotal(BigDecimal.ZERO);
    invoice.setTaxTotal(BigDecimal.ZERO);
    invoice.setInTaxTotal(BigDecimal.ZERO);

    // In the company accounting currency
    invoice.setCompanyExTaxTotal(BigDecimal.ZERO);
    invoice.setCompanyTaxTotal(BigDecimal.ZERO);
    invoice.setCompanyInTaxTotal(BigDecimal.ZERO);

    if (CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
      invoice.setInvoiceLineList(new ArrayList<InvoiceLine>());
    }

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {

      if (invoiceLine.getTypeSelect() != InvoiceLineRepository.TYPE_NORMAL) {
        continue;
      }

      // In the invoice currency
      invoice.setExTaxTotal(
          currencyScaleServiceAccount.getScaledValue(
              invoice, invoice.getExTaxTotal().add(invoiceLine.getExTaxTotal())));

      // In the company accounting currency
      invoice.setCompanyExTaxTotal(
          currencyScaleServiceAccount.getCompanyScaledValue(
              invoice, invoice.getCompanyExTaxTotal().add(invoiceLine.getCompanyExTaxTotal())));
    }

    if (invoice.getInvoiceLineTaxList() == null) {
      invoice.setInvoiceLineTaxList(new ArrayList<InvoiceLineTax>());
    } else {
      invoice.getInvoiceLineTaxList().clear();
    }
    List<InvoiceLineTax> invoiceTaxLines =
        (new TaxInvoiceLine(invoice, invoice.getInvoiceLineList())).creates();

    invoice.getInvoiceLineTaxList().addAll(invoiceTaxLines);

    for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()) {

      // In the invoice currency
      invoice.setTaxTotal(
          currencyScaleServiceAccount.getScaledValue(
              invoice, invoice.getTaxTotal().add(invoiceLineTax.getTaxTotal())));

      // In the company accounting currency
      invoice.setCompanyTaxTotal(
          currencyScaleServiceAccount.getCompanyScaledValue(
              invoice, invoice.getCompanyTaxTotal().add(invoiceLineTax.getCompanyTaxTotal())));
    }

    // In the invoice currency
    invoice.setInTaxTotal(
        currencyScaleServiceAccount.getScaledValue(
            invoice, invoice.getExTaxTotal().add(invoice.getTaxTotal())));

    // In the company accounting currency
    invoice.setCompanyInTaxTotal(
        currencyScaleServiceAccount.getCompanyScaledValue(
            invoice, invoice.getCompanyExTaxTotal().add(invoice.getCompanyTaxTotal())));
    invoice.setCompanyInTaxTotalRemaining(invoice.getCompanyInTaxTotal());

    invoice.setAmountRemaining(invoice.getInTaxTotal());

    invoice.setHasPendingPayments(false);

    if (!ObjectUtils.isEmpty(invoice.getInvoiceLineList())
        && ObjectUtils.isEmpty(invoice.getInvoiceTermList())) {
      Beans.get(InvoiceTermService.class).computeInvoiceTerms(invoice);
    }
  }
}
