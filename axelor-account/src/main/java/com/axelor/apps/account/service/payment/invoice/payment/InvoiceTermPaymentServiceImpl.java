/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermFilterService;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
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
  protected InvoicePaymentToolService invoicePaymentToolService;

  @Inject
  public InvoiceTermPaymentServiceImpl(
      CurrencyService currencyService,
      AppAccountService appAccountService,
      CurrencyScaleService currencyScaleService,
      InvoicePaymentFinancialDiscountService invoicePaymentFinancialDiscountService,
      InvoiceTermToolService invoiceTermToolService,
      InvoiceTermFilterService invoiceTermFilterService,
      InvoicePaymentToolService invoicePaymentToolService) {
    this.currencyService = currencyService;
    this.appAccountService = appAccountService;
    this.currencyScaleService = currencyScaleService;
    this.invoicePaymentFinancialDiscountService = invoicePaymentFinancialDiscountService;
    this.invoiceTermToolService = invoiceTermToolService;
    this.invoiceTermFilterService = invoiceTermFilterService;
    this.invoicePaymentToolService = invoicePaymentToolService;
  }

  @Override
  public InvoicePayment initInvoiceTermPayments(
      InvoicePayment invoicePayment, List<InvoiceTerm> invoiceTermsToPay, LocalDate paymentDate)
      throws AxelorException {
    invoicePayment.clearInvoiceTermPaymentList();

    if (CollectionUtils.isEmpty(invoiceTermsToPay)) {
      return invoicePayment;
    }

    for (InvoiceTerm invoiceTerm : invoiceTermsToPay) {
      BigDecimal companyAmount =
          invoicePaymentToolService.computeCompanyAmount(
              invoiceTerm.getAmountRemaining(),
              invoiceTerm.getCurrency(),
              invoiceTerm.getCompanyCurrency(),
              invoicePayment.getPaymentDate());

      invoicePayment.addInvoiceTermPaymentListItem(
          createInvoiceTermPayment(
              invoicePayment, invoiceTerm, invoiceTerm.getAmountRemaining(), companyAmount));
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
    } else {
      invoiceTerms = invoiceTermToolService.getPaymentVoucherInvoiceTerms(invoicePayment, invoice);
    }

    if (CollectionUtils.isNotEmpty(invoiceTerms)) {
      BigDecimal companyAmount =
          currencyService.getAmountCurrencyConvertedAtDate(
              invoicePayment.getCurrency(),
              invoicePayment.getCompanyCurrency(),
              invoicePayment.getAmount(),
              invoicePayment.getPaymentDate());
      this.initInvoiceTermPaymentsWithAmount(
          invoicePayment, invoiceTerms, companyAmount, invoicePayment.getAmount(), companyAmount);
    }
  }

  @Override
  public List<InvoiceTermPayment> initInvoiceTermPaymentsWithAmount(
      InvoicePayment invoicePayment,
      List<InvoiceTerm> invoiceTermsToPay,
      BigDecimal availableAmount,
      BigDecimal currencyAvailableAmount,
      BigDecimal reconcileAmount) {
    List<InvoiceTermPayment> invoiceTermPaymentList = new ArrayList<>();
    InvoiceTerm invoiceTermToPay;
    InvoiceTermPayment invoiceTermPayment;
    BigDecimal baseAvailableAmount = availableAmount;
    int invoiceTermCount = invoiceTermsToPay.size();

    if (invoicePayment != null) {
      invoicePayment.clearInvoiceTermPaymentList();
    }

    int i = 0;
    while (i < invoiceTermCount
        && availableAmount.signum() > 0
        && currencyAvailableAmount.signum() > 0) {
      invoiceTermToPay =
          this.getInvoiceTermToPay(
              invoicePayment, invoiceTermsToPay, currencyAvailableAmount, i++, invoiceTermCount);

      if (invoiceTermToPay.getPfpValidateStatusSelect()
          != InvoiceTermRepository.PFP_STATUS_LITIGATION) {
        BigDecimal invoiceTermCompanyAmount = invoiceTermToPay.getCompanyAmountRemaining();
        BigDecimal invoiceTermAmount = invoiceTermToPay.getAmountRemaining();
        if (invoiceTermAmount.compareTo(currencyAvailableAmount) >= 0
            || invoiceTermCompanyAmount.compareTo(availableAmount) >= 0
            || i == invoiceTermCount) {
          invoiceTermPayment =
              createInvoiceTermPayment(
                  invoicePayment, invoiceTermToPay, currencyAvailableAmount, availableAmount);
          availableAmount = BigDecimal.ZERO;
          currencyAvailableAmount = BigDecimal.ZERO;
        } else {
          invoiceTermPayment =
              createInvoiceTermPayment(
                  invoicePayment, invoiceTermToPay, invoiceTermAmount, invoiceTermCompanyAmount);
          availableAmount = availableAmount.subtract(invoiceTermCompanyAmount);
          currencyAvailableAmount = currencyAvailableAmount.subtract(invoiceTermAmount);
        }

        invoiceTermPaymentList.add(invoiceTermPayment);

        if (invoicePayment != null) {
          invoicePayment.addInvoiceTermPaymentListItem(invoiceTermPayment);

          if (invoicePayment.getApplyFinancialDiscount()) {
            BigDecimal previousAmount =
                invoicePayment.getAmount().add(invoicePayment.getFinancialDiscountTotalAmount());
            invoicePaymentFinancialDiscountService.computeFinancialDiscountFields(invoicePayment);
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
                  it.getAmount().compareTo(amount) == 0
                      || it.getAmountRemaining().compareTo(amount) == 0)
          .findAny()
          .orElse(invoiceTermsToPay.get(counter));
    }
  }

  @Override
  public InvoiceTermPayment createInvoiceTermPayment(
      InvoicePayment invoicePayment,
      InvoiceTerm invoiceTermToPay,
      BigDecimal paidAmount,
      BigDecimal companyPaidAmount) {
    if (invoicePayment == null) {
      return this.initInvoiceTermPayment(invoiceTermToPay, paidAmount, companyPaidAmount);
    } else {
      this.toggleFinancialDiscount(invoicePayment, invoiceTermToPay);
      return this.initInvoiceTermPayment(
          invoicePayment,
          invoiceTermToPay,
          paidAmount,
          companyPaidAmount,
          invoicePayment.getApplyFinancialDiscount());
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
      InvoiceTerm invoiceTermToPay, BigDecimal amount, BigDecimal companyAmount) {
    return initInvoiceTermPayment(
        null,
        invoiceTermToPay,
        amount,
        companyAmount,
        invoiceTermToPay.getApplyFinancialDiscount());
  }

  protected InvoiceTermPayment initInvoiceTermPayment(
      InvoicePayment invoicePayment,
      InvoiceTerm invoiceTermToPay,
      BigDecimal paidAmount,
      BigDecimal companyPaidAmount,
      boolean applyFinancialDiscount) {
    InvoiceTermPayment invoiceTermPayment = new InvoiceTermPayment();

    invoiceTermPayment.setInvoicePayment(invoicePayment);
    invoiceTermPayment.setInvoiceTerm(invoiceTermToPay);
    invoiceTermPayment.setPaidAmount(paidAmount);

    if (companyPaidAmount.compareTo(invoiceTermToPay.getAmount()) == 0
        || companyPaidAmount.compareTo(invoiceTermToPay.getRemainingAmountAfterFinDiscount())
            == 0) {
      manageInvoiceTermFinancialDiscount(
          invoiceTermPayment, invoiceTermToPay, applyFinancialDiscount);
    }

    invoiceTermPayment.setCompanyPaidAmount(companyPaidAmount);
    return invoiceTermPayment;
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

      BigDecimal ratioPaid = BigDecimal.ONE;
      boolean isFinancialDiscountAlreadyComputed = true;
      if (invoiceTerm
              .getRemainingAmountAfterFinDiscount()
              .compareTo(invoiceTermPayment.getPaidAmount())
          != 0) {
        ratioPaid =
            invoiceTermPayment
                .getPaidAmount()
                .divide(
                    invoiceTerm.getAmount(),
                    AppBaseService.COMPUTATION_SCALING,
                    RoundingMode.HALF_UP);
        isFinancialDiscountAlreadyComputed = false;
      }

      invoiceTermPayment.setFinancialDiscountAmount(
          currencyScaleService.getScaledValue(
              invoiceTerm, invoiceTerm.getFinancialDiscountAmount().multiply(ratioPaid)));

      if (!isFinancialDiscountAlreadyComputed) {
        invoiceTermPayment.setPaidAmount(
            currencyScaleService.getScaledValue(
                invoiceTerm,
                invoiceTermPayment
                    .getPaidAmount()
                    .subtract(invoiceTermPayment.getFinancialDiscountAmount())));
      }
    }
  }

  @Override
  public InvoicePayment updateInvoicePaymentAmount(InvoicePayment invoicePayment, Invoice invoice)
      throws AxelorException {

    invoicePayment.setAmount(
        computeInvoicePaymentAmount(
            invoicePayment, invoicePayment.getInvoiceTermPaymentList(), invoice));

    return invoicePayment;
  }

  @Override
  public BigDecimal computeInvoicePaymentAmount(
      InvoicePayment invoicePayment, List<InvoiceTermPayment> invoiceTermPayments, Invoice invoice)
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

  @Override
  public List<Long> initializeInvoiceTermPaymentWithoutDiscount(InvoicePayment invoicePayment)
      throws AxelorException {
    List<InvoiceTerm> invoiceTerms =
        invoiceTermFilterService.getUnpaidInvoiceTermsFiltered(invoicePayment.getInvoice());

    List<Long> invoiceTermIdList =
        invoiceTerms.stream().map(InvoiceTerm::getId).collect(Collectors.toList());

    if (!invoicePayment.getApplyFinancialDiscount()) {
      invoicePayment.setAmount(invoicePayment.getTotalAmountWithFinancialDiscount());
    }
    invoicePayment.clearInvoiceTermPaymentList();
    BigDecimal companyAmount =
        currencyService.getAmountCurrencyConvertedAtDate(
            invoicePayment.getCurrency(),
            invoicePayment.getCompanyCurrency(),
            invoicePayment.getAmount(),
            invoicePayment.getPaymentDate());
    this.initInvoiceTermPaymentsWithAmount(
        invoicePayment, invoiceTerms, companyAmount, invoicePayment.getAmount(), companyAmount);

    return invoiceTermIdList;
  }

  @Override
  public List<Long> applyFinancialDiscount(InvoicePayment invoicePayment, Long invoiceId)
      throws AxelorException {
    List<Long> invoiceTermIdList = null;

    if (invoiceId > 0) {
      invoiceTermIdList = initializeInvoiceTermPaymentWithoutDiscount(invoicePayment);

      invoicePaymentFinancialDiscountService.computeFinancialDiscountFields(invoicePayment);
    }
    return invoiceTermIdList;
  }
}
