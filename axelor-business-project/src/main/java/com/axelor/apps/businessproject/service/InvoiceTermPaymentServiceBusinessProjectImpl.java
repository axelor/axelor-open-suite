package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermFilterService;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentFinancialDiscountService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentServiceImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class InvoiceTermPaymentServiceBusinessProjectImpl extends InvoiceTermPaymentServiceImpl {

  @Inject
  public InvoiceTermPaymentServiceBusinessProjectImpl(
      CurrencyService currencyService,
      AppAccountService appAccountService,
      CurrencyScaleService currencyScaleService,
      InvoicePaymentFinancialDiscountService invoicePaymentFinancialDiscountService,
      InvoiceTermToolService invoiceTermToolService,
      InvoiceTermFilterService invoiceTermFilterService) {
    super(
        currencyService,
        appAccountService,
        currencyScaleService,
        invoicePaymentFinancialDiscountService,
        invoiceTermToolService,
        invoiceTermFilterService);
  }

  @Override
  public BigDecimal computeInvoicePaymentAmount(
      InvoicePayment invoicePayment, List<InvoiceTermPayment> invoiceTermPayments)
      throws AxelorException {

    BigDecimal sum =
        invoicePayment.getInvoiceTermPaymentList().stream()
            .map(it -> it.getPaidAmount().add(it.getFinancialDiscountAmount()))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    sum =
        currencyScaleService.getScaledValue(
            invoicePayment,
            currencyService.getAmountCurrencyConvertedAtDate(
                invoicePayment.getInvoice().getCurrency(),
                invoicePayment.getCurrency(),
                sum,
                appAccountService.getTodayDate(invoicePayment.getInvoice().getCompany())));

    return sum.subtract(invoicePayment.getInvoice().getHoldBacksTotal().abs());
  }
}
