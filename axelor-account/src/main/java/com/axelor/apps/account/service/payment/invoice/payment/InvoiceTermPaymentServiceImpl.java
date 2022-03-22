/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.PayVoucherElementToPay;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceTermPaymentServiceImpl implements InvoiceTermPaymentService {

  protected CurrencyService currencyService;
  protected InvoiceTermService invoiceTermService;
  protected AppAccountService appAccountService;

  @Inject
  public InvoiceTermPaymentServiceImpl(
      CurrencyService currencyService,
      InvoiceTermService invoiceTermService,
      AppAccountService appAccountService) {
    this.currencyService = currencyService;
    this.invoiceTermService = invoiceTermService;
    this.appAccountService = appAccountService;
  }

  @Override
  public InvoicePayment initInvoiceTermPayments(
      InvoicePayment invoicePayment, List<InvoiceTerm> invoiceTermsToPay) {

    invoicePayment.clearInvoiceTermPaymentList();
    for (InvoiceTerm invoiceTerm : invoiceTermsToPay) {
      invoicePayment.addInvoiceTermPaymentListItem(
          createInvoiceTermPayment(
              invoicePayment,
              invoiceTerm,
              invoiceTermService.getAmountRemaining(invoiceTerm, invoicePayment.getPaymentDate())));
    }

    return invoicePayment;
  }

  @Override
  public void createInvoicePaymentTerms(InvoicePayment invoicePayment) throws AxelorException {

    Invoice invoice = invoicePayment.getInvoice();
    if (invoice == null
        || CollectionUtils.isEmpty(invoicePayment.getInvoice().getInvoiceTermList())) {
      return;
    }

    List<InvoiceTerm> invoiceTerms;
    if (invoicePayment.getMove() != null
        && invoicePayment.getMove().getPaymentVoucher() != null
        && CollectionUtils.isNotEmpty(
            invoicePayment.getMove().getPaymentVoucher().getPayVoucherElementToPayList())) {
      invoiceTerms =
          invoicePayment.getMove().getPaymentVoucher().getPayVoucherElementToPayList().stream()
              .sorted(Comparator.comparing(PayVoucherElementToPay::getSequence))
              .map(PayVoucherElementToPay::getInvoiceTerm)
              .collect(Collectors.toList());
    } else {
      invoiceTerms = invoiceTermService.getUnpaidInvoiceTermsFiltered(invoice);
    }

    if (CollectionUtils.isNotEmpty(invoiceTerms)) {
      this.initInvoiceTermPaymentsWithAmount(
          invoicePayment, invoiceTerms, invoicePayment.getAmount());
    }
  }

  @Override
  public List<InvoiceTermPayment> initInvoiceTermPaymentsWithAmount(
      InvoicePayment invoicePayment,
      List<InvoiceTerm> invoiceTermsToPay,
      BigDecimal availableAmount) {
    int invoiceTermCount = invoiceTermsToPay.size();
    InvoicePaymentToolService invoicePaymentToolService =
        Beans.get(InvoicePaymentToolService.class);
    List<InvoiceTermPayment> invoiceTermPaymentList = new ArrayList<>();
    InvoiceTerm invoiceTermToPay;
    InvoiceTermPayment invoiceTermPayment;
    BigDecimal baseAvailableAmount = availableAmount;

    if (invoicePayment != null) {
      invoicePayment.clearInvoiceTermPaymentList();
    }

    int i = 0;
    while (i < invoiceTermCount && availableAmount.signum() > 0) {
      invoiceTermToPay =
          this.getInvoiceTermToPay(
              invoicePayment, invoiceTermsToPay, availableAmount, i++, invoiceTermCount);
      BigDecimal invoiceTermAmount =
          invoiceTermService.getAmountRemaining(
              invoiceTermToPay,
              invoicePayment != null
                  ? invoicePayment.getPaymentDate()
                  : appAccountService.getTodayDate(null));

      if (invoiceTermAmount.compareTo(availableAmount) >= 0) {
        invoiceTermPayment =
            createInvoiceTermPayment(invoicePayment, invoiceTermToPay, availableAmount);
        availableAmount = BigDecimal.ZERO;
      } else {
        invoiceTermPayment =
            createInvoiceTermPayment(invoicePayment, invoiceTermToPay, invoiceTermAmount);
        availableAmount = availableAmount.subtract(invoiceTermAmount);
      }

      invoiceTermPaymentList.add(invoiceTermPayment);

      if (invoicePayment != null) {
        invoicePayment.addInvoiceTermPaymentListItem(invoiceTermPayment);

        if (invoicePayment.getApplyFinancialDiscount()) {
          BigDecimal previousAmount =
              invoicePayment.getAmount().add(invoicePayment.getFinancialDiscountTotalAmount());
          invoicePaymentToolService.computeFinancialDiscount(invoicePayment);
          availableAmount = baseAvailableAmount.subtract(invoicePayment.getAmount());
          invoicePayment.setAmount(
              previousAmount.subtract(invoicePayment.getFinancialDiscountTotalAmount()));
          invoicePayment.setTotalAmountWithFinancialDiscount(
              invoicePayment.getAmount().add(invoicePayment.getFinancialDiscountTotalAmount()));
        }
      }
    }

    return invoiceTermPaymentList;
  }

  protected InvoiceTerm getInvoiceTermToPay(
      InvoicePayment invoicePayment,
      List<InvoiceTerm> invoiceTermsToPay,
      BigDecimal amount,
      int counter,
      int size) {
    if (invoicePayment != null) {
      return invoiceTermsToPay.get(counter);
    } else {
      return invoiceTermsToPay.subList(counter, size).stream()
          .filter(
              it ->
                  it.getAmount().compareTo(amount) == 0
                      || it.getAmountRemaining().compareTo(amount) == 0)
          .findAny()
          .orElse(invoiceTermsToPay.get(counter));
    }
  }

  @Override
  public InvoiceTermPayment createInvoiceTermPayment(
      InvoicePayment invoicePayment, InvoiceTerm invoiceTermToPay, BigDecimal paidAmount) {
    if (invoicePayment == null) {
      return this.initInvoiceTermPayment(invoiceTermToPay, paidAmount);
    } else {
      this.toggleFinancialDiscount(invoicePayment, invoiceTermToPay);
      return this.initInvoiceTermPayment(
          invoicePayment, invoiceTermToPay, paidAmount, invoicePayment.getApplyFinancialDiscount());
    }
  }

  protected void toggleFinancialDiscount(InvoicePayment invoicePayment, InvoiceTerm invoiceTerm) {
    if (!invoicePayment.getApplyFinancialDiscount()) {
      invoicePayment.setApplyFinancialDiscount(
          invoiceTerm.getApplyFinancialDiscount()
              && !invoicePayment
                  .getPaymentDate()
                  .isAfter(invoiceTerm.getFinancialDiscountDeadlineDate()));
    }
  }

  protected InvoiceTermPayment initInvoiceTermPayment(
      InvoiceTerm invoiceTermToPay, BigDecimal amount) {
    return initInvoiceTermPayment(null, invoiceTermToPay, amount, false);
  }

  protected InvoiceTermPayment initInvoiceTermPayment(
      InvoicePayment invoicePayment,
      InvoiceTerm invoiceTermToPay,
      BigDecimal paidAmount,
      boolean applyFinancialDiscount) {
    InvoiceTermPayment invoiceTermPayment = new InvoiceTermPayment();

    invoiceTermPayment.setInvoicePayment(invoicePayment);
    invoiceTermPayment.setInvoiceTerm(invoiceTermToPay);
    invoiceTermPayment.setPaidAmount(paidAmount);

    manageInvoiceTermFinancialDiscount(
        invoiceTermPayment, invoiceTermToPay, applyFinancialDiscount);

    return invoiceTermPayment;
  }

  @Override
  public void manageInvoiceTermFinancialDiscount(
      InvoiceTermPayment invoiceTermPayment,
      InvoiceTerm invoiceTerm,
      boolean applyFinancialDiscount) {
    if (applyFinancialDiscount) {
      BigDecimal remainingTotalFinancialDiscountAmount =
          invoiceTerm
              .getAmountRemaining()
              .subtract(invoiceTerm.getAmountRemainingAfterFinDiscount());
      BigDecimal ratioPaid =
          invoiceTermPayment
              .getPaidAmount()
              .divide(invoiceTerm.getAmountRemainingAfterFinDiscount(), 10, RoundingMode.HALF_UP);

      invoiceTermPayment.setFinancialDiscountAmount(
          remainingTotalFinancialDiscountAmount
              .multiply(ratioPaid)
              .setScale(2, RoundingMode.HALF_UP));
    }
  }

  @Override
  public InvoicePayment updateInvoicePaymentAmount(InvoicePayment invoicePayment)
      throws AxelorException {

    invoicePayment.setAmount(
        computeInvoicePaymentAmount(invoicePayment, invoicePayment.getInvoiceTermPaymentList()));

    return invoicePayment;
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
        currencyService
            .getAmountCurrencyConvertedAtDate(
                invoicePayment.getInvoice().getCurrency(),
                invoicePayment.getCurrency(),
                sum,
                appAccountService.getTodayDate(invoicePayment.getInvoice().getCompany()))
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);

    return sum;
  }
}
