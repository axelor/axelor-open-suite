package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.service.invoice.InvoiceTermFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InvoicePaymentFinancialDiscountServiceImpl
    implements InvoicePaymentFinancialDiscountService {
  protected InvoiceTermService invoiceTermService;
  protected InvoiceTermPaymentService invoiceTermPaymentService;
  protected InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService;

  @Inject
  public InvoicePaymentFinancialDiscountServiceImpl(
      InvoiceTermService invoiceTermService,
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService) {
    this.invoiceTermService = invoiceTermService;
    this.invoiceTermPaymentService = invoiceTermPaymentService;
    this.invoiceTermFinancialDiscountService = invoiceTermFinancialDiscountService;
  }

  @Override
  public void computeFinancialDiscount(InvoicePayment invoicePayment) {
    if (CollectionUtils.isEmpty(invoicePayment.getInvoiceTermPaymentList())) {
      if (invoicePayment.getApplyFinancialDiscount()) {
        this.resetFinancialDiscount(invoicePayment);
      }

      return;
    }

    List<InvoiceTermPayment> invoiceTermPaymentList =
        invoicePayment.getInvoiceTermPaymentList().stream()
            .filter(
                it ->
                    it.getInvoiceTerm() != null
                        && it.getInvoiceTerm().getApplyFinancialDiscount()
                        && !invoiceTermService.isPartiallyPaid(it.getInvoiceTerm())
                        && !invoicePayment
                            .getPaymentDate()
                            .isAfter(it.getInvoiceTerm().getFinancialDiscountDeadlineDate()))
            .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(invoiceTermPaymentList)) {
      invoicePayment.setApplyFinancialDiscount(false);
      this.resetFinancialDiscount(invoicePayment);

      return;
    }

    if (!invoicePayment.getManualChange()) {
      invoicePayment.setApplyFinancialDiscount(true);
    }
    invoicePayment.setFinancialDiscount(
        invoiceTermPaymentList.get(0).getInvoiceTerm().getFinancialDiscount());
    invoicePayment.setFinancialDiscountTotalAmount(
        this.getFinancialDiscountTotalAmount(invoiceTermPaymentList));
    invoicePayment.setFinancialDiscountTaxAmount(
        this.getFinancialDiscountTaxAmount(invoiceTermPaymentList));
    invoicePayment.setFinancialDiscountAmount(
        invoicePayment
            .getFinancialDiscountTotalAmount()
            .subtract(invoicePayment.getFinancialDiscountTaxAmount()));
    invoicePayment.setTotalAmountWithFinancialDiscount(
        invoicePayment.getAmount().add(invoicePayment.getFinancialDiscountTotalAmount()));
    invoicePayment.setFinancialDiscountDeadlineDate(
        this.getFinancialDiscountDeadlineDate(invoiceTermPaymentList));
  }

  protected void resetFinancialDiscount(InvoicePayment invoicePayment) {
    invoicePayment.setFinancialDiscountTotalAmount(BigDecimal.ZERO);
    invoicePayment.setFinancialDiscountTaxAmount(BigDecimal.ZERO);
    invoicePayment.setFinancialDiscountAmount(BigDecimal.ZERO);
    invoicePayment.setTotalAmountWithFinancialDiscount(BigDecimal.ZERO);
  }

  protected BigDecimal getFinancialDiscountTotalAmount(
      List<InvoiceTermPayment> invoiceTermPaymentList) {
    return invoiceTermPaymentList.stream()
        .map(InvoiceTermPayment::getFinancialDiscountAmount)
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO)
        .setScale(2, RoundingMode.HALF_UP);
  }

  protected BigDecimal getFinancialDiscountTaxAmount(
      List<InvoiceTermPayment> invoiceTermPaymentList) {
    return invoiceTermPaymentList.stream()
        .filter(it -> it.getInvoiceTerm().getAmountRemainingAfterFinDiscount().signum() > 0)
        .map(
            it -> {
              try {
                return invoiceTermFinancialDiscountService
                    .getFinancialDiscountTaxAmount(it.getInvoiceTerm())
                    .multiply(it.getPaidAmount())
                    .divide(
                        it.getInvoiceTerm().getAmountRemainingAfterFinDiscount(),
                        10,
                        RoundingMode.HALF_UP);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO)
        .setScale(2, RoundingMode.HALF_UP);
  }

  protected LocalDate getFinancialDiscountDeadlineDate(
      List<InvoiceTermPayment> invoiceTermPaymentList) {
    return invoiceTermPaymentList.stream()
        .map(InvoiceTermPayment::getInvoiceTerm)
        .map(InvoiceTerm::getFinancialDiscountDeadlineDate)
        .min(LocalDate::compareTo)
        .orElse(null);
  }

  @Override
  public List<Long> computeDataForFinancialDiscount(InvoicePayment invoicePayment, Long invoiceId)
      throws AxelorException {
    List<Long> invoiceTermIdList = null;

    if (invoiceId > 0) {
      List<InvoiceTerm> invoiceTerms =
          invoiceTermService.getUnpaidInvoiceTermsFiltered(invoicePayment.getInvoice());

      invoiceTermIdList =
          invoiceTerms.stream().map(InvoiceTerm::getId).collect(Collectors.toList());

      if (!invoicePayment.getApplyFinancialDiscount()) {
        invoicePayment.setAmount(invoicePayment.getTotalAmountWithFinancialDiscount());
      }
      invoicePayment.clearInvoiceTermPaymentList();
      invoiceTermPaymentService.initInvoiceTermPaymentsWithAmount(
          invoicePayment, invoiceTerms, invoicePayment.getAmount(), invoicePayment.getAmount());

      this.computeFinancialDiscount(invoicePayment);

      if (invoicePayment.getApplyFinancialDiscount()) {
        invoicePayment.setTotalAmountWithFinancialDiscount(invoicePayment.getAmount());

        invoicePayment.setAmount(
            invoicePayment.getAmount().subtract(invoicePayment.getFinancialDiscountTotalAmount()));
      }
    }
    return invoiceTermIdList;
  }
}
