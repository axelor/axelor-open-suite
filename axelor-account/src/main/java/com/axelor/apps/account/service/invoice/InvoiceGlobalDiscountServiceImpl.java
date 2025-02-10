package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.discount.GlobalDiscountService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class InvoiceGlobalDiscountServiceImpl implements InvoiceGlobalDiscountService {

  protected final GlobalDiscountService globalDiscountService;

  protected final InvoiceService invoiceService;
  protected final InvoiceLineService invoiceLineService;

  @Inject
  public InvoiceGlobalDiscountServiceImpl(
      GlobalDiscountService globalDiscountService,
      InvoiceService invoiceService,
      InvoiceLineService invoiceLineService) {
    this.globalDiscountService = globalDiscountService;
    this.invoiceService = invoiceService;
    this.invoiceLineService = invoiceLineService;
  }

  @Override
  public void applyGlobalDiscountOnLines(Invoice invoice) throws AxelorException {
    if (invoice == null
        || invoice.getInvoiceLineList() == null
        || invoice.getInvoiceLineList().isEmpty()) {
      return;
    }
    computePriceBeforeGlobalDiscount(invoice);
    switch (invoice.getDiscountTypeSelect()) {
      case PriceListLineRepository.AMOUNT_TYPE_PERCENT:
        applyPercentageGlobalDiscountOnLines(invoice);
        break;
      case PriceListLineRepository.AMOUNT_TYPE_FIXED:
        applyFixedGlobalDiscountOnLines(invoice);
        break;
    }
  }

  protected void computePriceBeforeGlobalDiscount(Invoice invoice) {
    invoice.setPriceBeforeGlobalDiscount(
        invoice.getInvoiceLineList().stream()
            .map(invoiceLine -> invoiceLine.getPrice().multiply(invoiceLine.getQty()))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO));
  }

  protected void applyPercentageGlobalDiscountOnLines(Invoice invoice) throws AxelorException {
    invoice.getInvoiceLineList().stream()
        .filter(
            invoiceLine -> invoiceLine.getTypeSelect().equals(InvoiceLineRepository.TYPE_NORMAL))
        .forEach(
            invoiceLine -> {
              invoiceLine.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_PERCENT);
              invoiceLine.setDiscountAmount(invoice.getDiscountAmount());
            });
    adjustPercentageDiscountOnLastLine(invoice);
  }

  protected void applyFixedGlobalDiscountOnLines(Invoice invoice) throws AxelorException {
    invoice.getInvoiceLineList().stream()
        .filter(
            invoiceLine -> invoiceLine.getTypeSelect().equals(InvoiceLineRepository.TYPE_NORMAL))
        .forEach(
            invoiceLine -> {
              invoiceLine.setDiscountTypeSelect(invoice.getDiscountTypeSelect());
              invoiceLine.setDiscountAmount(
                  invoiceLine
                      .getPrice()
                      .divide(invoice.getPriceBeforeGlobalDiscount(), RoundingMode.HALF_UP)
                      .multiply(invoice.getDiscountAmount()));
            });
    adjustFixedDiscountOnLastLine(invoice);
  }

  protected void adjustPercentageDiscountOnLastLine(Invoice invoice) throws AxelorException {
    computeInvoiceLine(invoice);
    BigDecimal priceDiscountedByLine = invoice.getExTaxTotal();
    BigDecimal priceDiscountedOnTotal =
        invoice
            .getPriceBeforeGlobalDiscount()
            .multiply(BigDecimal.valueOf(100).subtract(invoice.getDiscountAmount()))
            .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    if (priceDiscountedByLine.compareTo(priceDiscountedOnTotal) == 0) {
      return;
    }
    BigDecimal differenceInDiscount = priceDiscountedOnTotal.subtract(priceDiscountedByLine);

    InvoiceLine lastLine =
        invoice.getInvoiceLineList().get(invoice.getInvoiceLineList().size() - 1);

    lastLine.setDiscountAmount(
        BigDecimal.ONE
            .subtract(
                lastLine
                    .getPriceDiscounted()
                    .add(differenceInDiscount)
                    .divide(lastLine.getPrice(), RoundingMode.HALF_UP))
            .multiply(BigDecimal.valueOf(100)));
  }

  protected void adjustFixedDiscountOnLastLine(Invoice invoice) throws AxelorException {
    computeInvoiceLine(invoice);
    BigDecimal priceDiscountedByLine = invoice.getExTaxTotal();

    BigDecimal priceDiscountedOnTotal =
        invoice.getPriceBeforeGlobalDiscount().subtract(invoice.getDiscountAmount());
    if (priceDiscountedByLine.compareTo(priceDiscountedOnTotal) == 0) {
      return;
    }

    BigDecimal differenceInDiscount = priceDiscountedOnTotal.subtract(priceDiscountedByLine);
    InvoiceLine lastLine =
        invoice.getInvoiceLineList().get(invoice.getInvoiceLineList().size() - 1);
    lastLine.setDiscountAmount(
        lastLine
            .getDiscountAmount()
            .subtract(differenceInDiscount.divide(lastLine.getQty(), RoundingMode.HALF_UP)));
  }

  protected void computeInvoiceLine(Invoice invoice) throws AxelorException {
    invoice
        .getInvoiceLineList()
        .forEach(
            invoiceLine -> {
              try {
                invoiceLineService.compute(invoice, invoiceLine);
              } catch (AxelorException e) {
                throw new RuntimeException(e);
              }
            });
    invoiceService.compute(invoice);
  }

  @Override
  public BigDecimal computeDiscountFixedEquivalence(Invoice invoice) {
    if (invoice == null) {
      return BigDecimal.ZERO;
    }
    return globalDiscountService.computeDiscountFixedEquivalence(
        invoice.getExTaxTotal(), invoice.getPriceBeforeGlobalDiscount());
  }

  @Override
  public BigDecimal computeDiscountPercentageEquivalence(Invoice invoice) {
    if (invoice == null) {
      return BigDecimal.ZERO;
    }
    return globalDiscountService.computeDiscountPercentageEquivalence(
        invoice.getExTaxTotal(), invoice.getPriceBeforeGlobalDiscount());
  }

  @Override
  public Map<String, Map<String, Object>> setDiscountDummies(Invoice invoice) {
    if (invoice == null) {
      return null;
    }
    return globalDiscountService.setDiscountDummies(
        invoice.getDiscountTypeSelect(),
        invoice.getCurrency(),
        invoice.getExTaxTotal(),
        invoice.getPriceBeforeGlobalDiscount());
  }
}
