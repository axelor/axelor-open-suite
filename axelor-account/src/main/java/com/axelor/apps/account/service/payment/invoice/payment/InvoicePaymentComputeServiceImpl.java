package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.service.invoice.InvoiceTermFilterService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class InvoicePaymentComputeServiceImpl implements InvoicePaymentComputeService {

  protected InvoiceTermPaymentService invoiceTermPaymentService;
  protected InvoicePaymentFinancialDiscountService invoicePaymentFinancialDiscountService;
  protected InvoiceTermFilterService invoiceTermFilterService;

  @Inject
  public InvoicePaymentComputeServiceImpl(
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoicePaymentFinancialDiscountService invoicePaymentFinancialDiscountService,
      InvoiceTermFilterService invoiceTermFilterService) {
    this.invoiceTermPaymentService = invoiceTermPaymentService;
    this.invoicePaymentFinancialDiscountService = invoicePaymentFinancialDiscountService;
    this.invoiceTermFilterService = invoiceTermFilterService;
  }

  @Override
  public List<Long> computeDataForFinancialDiscount(InvoicePayment invoicePayment, Long invoiceId)
      throws AxelorException {
    List<Long> invoiceTermIdList = null;

    if (invoiceId > 0) {
      List<InvoiceTerm> invoiceTerms =
          invoiceTermFilterService.getUnpaidInvoiceTermsFiltered(invoicePayment.getInvoice());

      invoiceTermIdList =
          invoiceTerms.stream().map(InvoiceTerm::getId).collect(Collectors.toList());

      if (!invoicePayment.getApplyFinancialDiscount()) {
        invoicePayment.setAmount(invoicePayment.getTotalAmountWithFinancialDiscount());
      }
      invoicePayment.clearInvoiceTermPaymentList();
      invoiceTermPaymentService.initInvoiceTermPaymentsWithAmount(
          invoicePayment, invoiceTerms, invoicePayment.getAmount(), invoicePayment.getAmount());

      invoicePaymentFinancialDiscountService.computeFinancialDiscount(invoicePayment);

      if (invoicePayment.getApplyFinancialDiscount()) {
        invoicePayment.setTotalAmountWithFinancialDiscount(invoicePayment.getAmount());

        invoicePayment.setAmount(
            invoicePayment.getAmount().subtract(invoicePayment.getFinancialDiscountTotalAmount()));
      }
    }
    return invoiceTermIdList;
  }
}
