/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PayVoucherElementToPay;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermFilterService;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class InvoiceTermPaymentServiceImpl implements InvoiceTermPaymentService {

  protected CurrencyService currencyService;
  protected AppAccountService appAccountService;
  protected CurrencyScaleService currencyScaleService;
  protected InvoicePaymentFinancialDiscountService invoicePaymentFinancialDiscountService;
  protected InvoiceTermToolService invoiceTermToolService;
  protected InvoiceTermFilterService invoiceTermFilterService;

  @Inject
  public InvoiceTermPaymentServiceImpl(
      CurrencyService currencyService,
      AppAccountService appAccountService,
      CurrencyScaleService currencyScaleService,
      InvoicePaymentFinancialDiscountService invoicePaymentFinancialDiscountService,
      InvoiceTermToolService invoiceTermToolService,
      InvoiceTermFilterService invoiceTermFilterService) {
    this.currencyService = currencyService;
    this.appAccountService = appAccountService;
    this.currencyScaleService = currencyScaleService;
    this.invoicePaymentFinancialDiscountService = invoicePaymentFinancialDiscountService;
    this.invoiceTermToolService = invoiceTermToolService;
    this.invoiceTermFilterService = invoiceTermFilterService;
  }

  @Override
  public InvoicePayment initInvoiceTermPayments(
      InvoicePayment invoicePayment, List<InvoiceTerm> invoiceTermsToPay) {
    invoicePayment.clearInvoiceTermPaymentList();

    if (CollectionUtils.isEmpty(invoiceTermsToPay)) {
      return invoicePayment;
    }

    for (InvoiceTerm invoiceTerm : invoiceTermsToPay) {
      invoicePayment.addInvoiceTermPaymentListItem(
          createInvoiceTermPayment(
              invoicePayment,
              invoiceTerm,
              currencyScaleService.getCompanyScaledValue(
                  invoiceTerm, invoiceTerm.getCompanyAmountRemaining())));
    }

    return invoicePayment;
  }

  @Override
  public void createInvoicePaymentTerms(
      InvoicePayment invoicePayment, List<InvoiceTerm> invoiceTermToPayList)
      throws AxelorException {

    Invoice invoice = invoicePayment.getInvoice();
    if (invoice == null
        || CollectionUtils.isEmpty(invoicePayment.getInvoice().getInvoiceTermList())) {
      return;
    }

    List<InvoiceTerm> invoiceTerms;
    if (CollectionUtils.isNotEmpty(invoiceTermToPayList)) {
      invoiceTerms = new ArrayList<>(invoiceTermToPayList);
    } else if (invoicePayment.getMove() != null
        && invoicePayment.getMove().getPaymentVoucher() != null
        && CollectionUtils.isNotEmpty(
            invoicePayment.getMove().getPaymentVoucher().getPayVoucherElementToPayList())) {
      invoiceTerms =
          invoicePayment.getMove().getPaymentVoucher().getPayVoucherElementToPayList().stream()
              .sorted(Comparator.comparing(PayVoucherElementToPay::getSequence))
              .map(PayVoucherElementToPay::getInvoiceTerm)
              .collect(Collectors.toList());
    } else {
      invoiceTerms = invoiceTermFilterService.getUnpaidInvoiceTermsFiltered(invoice);
    }

    if (CollectionUtils.isNotEmpty(invoiceTerms)) {
      this.initInvoiceTermPaymentsWithAmount(
          invoicePayment, invoiceTerms, invoicePayment.getAmount(), invoicePayment.getAmount());
    }
  }

  @Override
  public List<InvoiceTermPayment> initInvoiceTermPaymentsWithAmount(
      InvoicePayment invoicePayment,
      List<InvoiceTerm> invoiceTermsToPay,
      BigDecimal availableAmount,
      BigDecimal reconcileAmount) {
    List<InvoiceTermPayment> invoiceTermPaymentList = new ArrayList<>();
    InvoiceTerm invoiceTermToPay;
    InvoiceTermPayment invoiceTermPayment;
    BigDecimal baseAvailableAmount = availableAmount;
    BigDecimal availableAmountUnchanged = availableAmount;
    int invoiceTermCount = invoiceTermsToPay.size();

    if (invoicePayment != null) {
      invoicePayment.clearInvoiceTermPaymentList();
    }

    int i = 0;
    while (i < invoiceTermCount && availableAmount.signum() > 0) {
      invoiceTermToPay =
          this.getInvoiceTermToPay(
              invoicePayment, invoiceTermsToPay, availableAmount, i++, invoiceTermCount);

      if (invoiceTermToPay.getPfpValidateStatusSelect()
          != InvoiceTermRepository.PFP_STATUS_LITIGATION) {
        BigDecimal invoiceTermCompanyAmount =
            currencyScaleService.getCompanyScaledValue(
                invoiceTermToPay, invoiceTermToPay.getCompanyAmountRemaining());

        if (invoiceTermCompanyAmount.compareTo(availableAmount) >= 0) {
          invoiceTermPayment =
              createInvoiceTermPayment(invoicePayment, invoiceTermToPay, availableAmount);
          availableAmount = BigDecimal.ZERO;
        } else {
          invoiceTermPayment =
              createInvoiceTermPayment(invoicePayment, invoiceTermToPay, invoiceTermCompanyAmount);
          availableAmount = availableAmount.subtract(invoiceTermCompanyAmount);
        }

        invoiceTermPaymentList.add(invoiceTermPayment);

        if (invoicePayment != null) {
          invoicePayment.addInvoiceTermPaymentListItem(invoiceTermPayment);

          if (invoicePayment.getApplyFinancialDiscount() && !invoicePayment.getManualChange()) {
            BigDecimal previousAmount =
                invoicePayment.getAmount().add(invoicePayment.getFinancialDiscountTotalAmount());
            invoicePaymentFinancialDiscountService.computeFinancialDiscount(invoicePayment);
            availableAmount =
                baseAvailableAmount.subtract(this.getCurrentInvoicePaymentAmount(invoicePayment));
            invoicePayment.setAmount(
                currencyScaleService.getCompanyScaledValue(
                    invoiceTermToPay,
                    previousAmount.subtract(invoicePayment.getFinancialDiscountTotalAmount())));
            invoicePayment.setTotalAmountWithFinancialDiscount(
                currencyScaleService.getCompanyScaledValue(
                    invoiceTermToPay,
                    invoicePayment
                        .getAmount()
                        .add(invoicePayment.getFinancialDiscountTotalAmount())));
          }
        }

        if (availableAmountUnchanged.compareTo(reconcileAmount) != 0
            && availableAmount.signum() <= 0) {
          BigDecimal totalInCompanyCurrency =
              invoiceTermPaymentList.stream()
                  .map(InvoiceTermPayment::getCompanyPaidAmount)
                  .reduce(BigDecimal::add)
                  .orElse(BigDecimal.ZERO);
          BigDecimal diff = reconcileAmount.subtract(totalInCompanyCurrency);
          BigDecimal companyPaidAmount =
              currencyScaleService.getCompanyScaledValue(
                  invoiceTermToPay, invoiceTermPayment.getCompanyPaidAmount().add(diff));

          invoiceTermPayment.setCompanyPaidAmount(companyPaidAmount);
        }
      }
    }

    return invoiceTermPaymentList;
  }

  protected BigDecimal getCurrentInvoicePaymentAmount(InvoicePayment invoicePayment) {
    return invoicePayment.getInvoiceTermPaymentList().stream()
        .map(it -> it.getPaidAmount().add(it.getFinancialDiscountAmount()))
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
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
                  it.getCompanyAmount().compareTo(amount) == 0
                      || it.getCompanyAmountRemaining().compareTo(amount) == 0)
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
    boolean isLinkedToPayment = true;
    if (invoicePayment.getReconcile() != null) {
      Reconcile reconcile = invoicePayment.getReconcile();
      isLinkedToPayment =
          reconcile.getDebitMoveLine().getMove().getFunctionalOriginSelect()
                  == MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT
              || reconcile.getCreditMoveLine().getMove().getFunctionalOriginSelect()
                  == MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT;
    }
    if (!invoicePayment.getApplyFinancialDiscount()
        && !invoicePayment.getManualChange()
        && Optional.of(invoicePayment)
            .map(InvoicePayment::getMove)
            .map(Move::getPaymentVoucher)
            .isEmpty()
        && (!invoiceTerm.getIsSelectedOnPaymentSession()
            || invoiceTerm.getApplyFinancialDiscountOnPaymentSession())
        && !invoiceTermToolService.isPartiallyPaid(invoiceTerm)) {
      invoicePayment.setApplyFinancialDiscount(
          invoiceTerm.getFinancialDiscountDeadlineDate() != null
              && invoiceTerm.getApplyFinancialDiscount()
              && invoicePayment.getPaymentDate() != null
              && !invoicePayment
                  .getPaymentDate()
                  .isAfter(invoiceTerm.getFinancialDiscountDeadlineDate())
              && isLinkedToPayment);
    }
  }

  protected InvoiceTermPayment initInvoiceTermPayment(
      InvoiceTerm invoiceTermToPay, BigDecimal amount) {
    return initInvoiceTermPayment(
        null, invoiceTermToPay, amount, invoiceTermToPay.getApplyFinancialDiscount());
  }

  protected InvoiceTermPayment initInvoiceTermPayment(
      InvoicePayment invoicePayment,
      InvoiceTerm invoiceTermToPay,
      BigDecimal companyPaidAmount,
      boolean applyFinancialDiscount) {
    InvoiceTermPayment invoiceTermPayment = new InvoiceTermPayment();

    try {
      invoiceTermPayment.setInvoicePayment(invoicePayment);
      invoiceTermPayment.setInvoiceTerm(invoiceTermToPay);
      invoiceTermPayment.setPaidAmount(
          this.computePaidAmount(companyPaidAmount, invoicePayment, invoiceTermToPay));

      if (companyPaidAmount.compareTo(invoiceTermToPay.getAmount()) == 0) {
        manageInvoiceTermFinancialDiscount(
            invoiceTermPayment, invoiceTermToPay, applyFinancialDiscount);
      }

      invoiceTermPayment.setCompanyPaidAmount(companyPaidAmount);
    } catch (AxelorException e) {
      TraceBackService.trace(
          new AxelorException(
              e,
              e.getCategory(),
              I18n.get("Invoice") + " %s",
              invoicePayment.getInvoice().getInvoiceId()),
          ExceptionOriginRepository.INVOICE_ORIGIN);
    }
    return invoiceTermPayment;
  }

  protected BigDecimal computePaidAmount(
      BigDecimal companyPaidAmount, InvoicePayment invoicePayment, InvoiceTerm invoiceTerm)
      throws AxelorException {
    Currency invoicePaymentCurrency = invoicePayment != null ? invoicePayment.getCurrency() : null;
    Currency companyCurrency = invoiceTerm.getCompanyCurrency();

    if (companyCurrency.equals(invoicePaymentCurrency)) {
      return companyPaidAmount;
    } else if (invoicePayment != null) {
      BigDecimal ratio =
          invoiceTerm
              .getAmount()
              .divide(
                  invoiceTerm.getCompanyAmount(),
                  AppBaseService.COMPUTATION_SCALING,
                  RoundingMode.HALF_UP);

      return currencyScaleService.getCompanyScaledValue(
          invoiceTerm, companyPaidAmount.multiply(ratio));
    }

    return BigDecimal.ZERO;
  }

  @Override
  public void manageInvoiceTermFinancialDiscount(
      InvoiceTermPayment invoiceTermPayment,
      InvoiceTerm invoiceTerm,
      boolean applyFinancialDiscount) {
    if (applyFinancialDiscount && invoiceTerm.getAmountRemainingAfterFinDiscount().signum() > 0) {
      invoiceTermPayment.setPaidAmount(
          currencyScaleService.getScaledValue(
              invoiceTerm,
              invoiceTermPayment
                  .getPaidAmount()
                  .add(invoiceTermPayment.getFinancialDiscountAmount())));

      BigDecimal ratioPaid =
          invoiceTermPayment
              .getPaidAmount()
              .divide(
                  invoiceTerm.getAmount(),
                  AppBaseService.COMPUTATION_SCALING,
                  RoundingMode.HALF_UP);

      invoiceTermPayment.setFinancialDiscountAmount(
          currencyScaleService.getScaledValue(
              invoiceTerm, invoiceTerm.getFinancialDiscountAmount().multiply(ratioPaid)));

      invoiceTermPayment.setPaidAmount(
          currencyScaleService.getScaledValue(
              invoiceTerm,
              invoiceTermPayment
                  .getPaidAmount()
                  .subtract(invoiceTermPayment.getFinancialDiscountAmount())));
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
        currencyScaleService.getScaledValue(
            invoicePayment,
            currencyService.getAmountCurrencyConvertedAtDate(
                invoicePayment.getInvoice().getCurrency(),
                invoicePayment.getCurrency(),
                sum,
                appAccountService.getTodayDate(invoicePayment.getInvoice().getCompany())));

    return sum;
  }
}
