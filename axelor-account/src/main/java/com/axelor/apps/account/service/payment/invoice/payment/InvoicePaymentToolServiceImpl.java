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
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class InvoicePaymentToolServiceImpl implements InvoicePaymentToolService {

  protected InvoiceRepository invoiceRepo;
  protected MoveToolService moveToolService;
  protected InvoicePaymentRepository invoicePaymentRepo;
  protected AccountConfigRepository accountConfigRepository;
  protected InvoiceTermService invoiceTermService;
  protected InvoiceTermPaymentService invoiceTermPaymentService;
  protected CurrencyService currencyService;
  protected AppAccountService appAccountService;

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public InvoicePaymentToolServiceImpl(
      InvoiceRepository invoiceRepo,
      MoveToolService moveToolService,
      InvoicePaymentRepository invoicePaymentRepo,
      InvoiceTermService invoiceTermService,
      InvoiceTermPaymentService invoiceTermPaymentService,
      CurrencyService currencyService,
      AppAccountService appAccountService) {

    this.invoiceRepo = invoiceRepo;
    this.moveToolService = moveToolService;
    this.invoicePaymentRepo = invoicePaymentRepo;
    this.invoiceTermService = invoiceTermService;
    this.invoiceTermPaymentService = invoiceTermPaymentService;
    this.currencyService = currencyService;
    this.appAccountService = appAccountService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateAmountPaid(Invoice invoice) throws AxelorException {

    invoice.setAmountPaid(computeAmountPaid(invoice));
    invoice.setAmountRemaining(invoice.getInTaxTotal().subtract(invoice.getAmountPaid()));
    invoice.setCompanyInTaxTotalRemaining(computeCompanyAmountRemaining(invoice));
    updateHasPendingPayments(invoice);
    updatePaymentProgress(invoice);
    invoiceRepo.save(invoice);
    log.debug("Invoice : {}, amount paid : {}", invoice.getInvoiceId(), invoice.getAmountPaid());
  }

  protected BigDecimal computeCompanyAmountRemaining(Invoice invoice) throws AxelorException {
    List<InvoiceTerm> invoiceTermList = invoice.getInvoiceTermList();
    BigDecimal amount = invoice.getAmountRemaining();
    if (!CollectionUtils.isEmpty(invoiceTermList)) {
      BigDecimal currencyAmount =
          invoiceTermList.stream()
              .map(InvoiceTerm::getCompanyAmountRemaining)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
      if (currencyAmount.signum() >= 0 || (currencyAmount.signum() == 0 && amount.signum() == 0)) {
        return currencyAmount;
      }
    }

    return currencyService.getAmountCurrencyConvertedAtDate(
        invoice.getCurrency(),
        invoice.getCompany().getCurrency(),
        amount,
        invoice.getInvoiceDate());
  }

  @Override
  @Transactional
  public void updateHasPendingPayments(Invoice invoice) {
    invoice.setHasPendingPayments(checkPendingPayments(invoice));
  }

  protected BigDecimal computeAmountPaid(Invoice invoice) throws AxelorException {

    BigDecimal amountPaid = BigDecimal.ZERO;

    if (invoice.getInvoicePaymentList() == null) {
      return amountPaid;
    }

    Currency invoiceCurrency = invoice.getCurrency();

    for (InvoicePayment invoicePayment : invoice.getInvoicePaymentList()) {

      if (invoicePayment.getStatusSelect() == InvoicePaymentRepository.STATUS_VALIDATED) {

        log.debug("Amount paid without move : {}", invoicePayment.getAmount());

        BigDecimal paymentAmount = invoicePayment.getAmount();
        if (invoicePayment.getApplyFinancialDiscount()) {
          paymentAmount =
              paymentAmount.add(
                  invoicePayment
                      .getFinancialDiscountAmount()
                      .add(invoicePayment.getFinancialDiscountTaxAmount()));
        }
        amountPaid =
            amountPaid.add(
                currencyService.getAmountCurrencyConvertedAtDate(
                    invoicePayment.getCurrency(),
                    invoiceCurrency,
                    paymentAmount,
                    invoicePayment.getPaymentDate()));
      }
    }

    boolean isMinus = moveToolService.isMinus(invoice);
    if (isMinus) {
      amountPaid = amountPaid.negate();
    }

    log.debug("Amount paid total : {}", amountPaid);

    return amountPaid;
  }

  /**
   * Check whether the sum of pending payments equals or exceeds the remaining amount of the
   * invoice.
   *
   * @param invoice
   * @return
   */
  protected boolean checkPendingPayments(Invoice invoice) {
    BigDecimal pendingAmount = BigDecimal.ZERO;

    if (invoice.getInvoicePaymentList() != null) {
      for (InvoicePayment invoicePayment : invoice.getInvoicePaymentList()) {
        if (invoicePayment.getStatusSelect() == InvoicePaymentRepository.STATUS_PENDING) {
          pendingAmount = pendingAmount.add(invoicePayment.getAmount());
        }
      }
    }
    BigDecimal remainingAmount =
        invoice.getFinancialDiscount() != null
            ? invoice.getRemainingAmountAfterFinDiscount()
            : invoice.getAmountRemaining();
    return remainingAmount.compareTo(pendingAmount) <= 0;
  }

  /** @inheritDoc */
  @Override
  public List<BankDetails> findCompatibleBankDetails(
      Company company, InvoicePayment invoicePayment) {
    PaymentMode paymentMode = invoicePayment.getPaymentMode();
    if (company == null || paymentMode == null) {
      return new ArrayList<BankDetails>();
    }
    paymentMode =
        Beans.get(PaymentModeRepository.class).find(invoicePayment.getPaymentMode().getId());
    return Beans.get(PaymentModeService.class).getCompatibleBankDetailsList(paymentMode, company);
  }

  @Override
  public List<InvoicePayment> assignAdvancePayment(Invoice invoice, Invoice advancePayment) {
    List<InvoicePayment> advancePaymentList = advancePayment.getInvoicePaymentList();
    if (advancePaymentList == null || advancePaymentList.isEmpty()) {
      return advancePaymentList;
    }

    for (InvoicePayment invoicePayment : advancePaymentList) {
      invoice.addInvoicePaymentListItem(invoicePayment);
    }
    return advancePaymentList;
  }

  @Override
  public List<MoveLine> getMoveLinesFromPayments(
      List<InvoicePayment> payments, boolean getCreditLines) {
    List<MoveLine> moveLines = new ArrayList<>();
    for (InvoicePayment payment : payments) {
      Move move = payment.getMove();
      if (move == null || move.getMoveLineList() == null || move.getMoveLineList().isEmpty()) {
        continue;
      }
      if (getCreditLines) {
        moveLines.addAll(moveToolService.getToReconcileCreditMoveLines(move));
      } else {
        moveLines.addAll(moveToolService.getToReconcileDebitMoveLines(move));
      }
    }
    return moveLines;
  }

  /**
   * Method who check if the payment is possible before validation
   *
   * @param invoicePayment the invoice payment to test
   * @throws AxelorException
   */
  @Override
  public void checkConditionBeforeSave(InvoicePayment invoicePayment) throws AxelorException {
    if (invoicePayment.getInvoice() != null
        && invoicePayment.getInvoice().getAmountRemaining().compareTo(BigDecimal.ZERO) <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.INVOICE_PAYMENT_NO_AMOUNT_REMAINING),
          invoicePayment.getInvoice().getInvoiceId());
    }
  }

  @Override
  public BigDecimal getPayableAmount(
      List<InvoiceTerm> invoiceTermList,
      LocalDate date,
      boolean manualChange,
      Currency paymentCurrency) {
    if (CollectionUtils.isEmpty(invoiceTermList)) {
      return BigDecimal.ZERO;
    }

    boolean isCompanyCurrency =
        paymentCurrency.equals(invoiceTermList.get(0).getCompany().getCurrency());
    return invoiceTermList.stream()
        .map(
            it ->
                manualChange
                    ? invoiceTermService.getAmountRemaining(it, date, isCompanyCurrency)
                    : isCompanyCurrency ? it.getCompanyAmountRemaining() : it.getAmountRemaining())
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }

  @Override
  public void computeFinancialDiscount(InvoicePayment invoicePayment) {
    if (CollectionUtils.isEmpty(invoicePayment.getInvoiceTermPaymentList())) {
      return;
    }

    List<InvoiceTermPayment> invoiceTermPaymentList =
        invoicePayment.getInvoiceTermPaymentList().stream()
            .filter(
                it ->
                    it.getInvoiceTerm() != null && it.getInvoiceTerm().getApplyFinancialDiscount())
            .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(invoiceTermPaymentList)) {
      invoicePayment.setApplyFinancialDiscount(false);
      return;
    }

    invoiceTermPaymentList.forEach(
        it ->
            invoiceTermPaymentService.manageInvoiceTermFinancialDiscount(
                it, it.getInvoiceTerm(), invoicePayment.getApplyFinancialDiscount()));

    invoicePayment.setApplyFinancialDiscount(true);
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
                return invoiceTermService
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
  public void updatePaymentProgress(Invoice invoice) {

    invoice.setPaymentProgress(
        invoice
            .getAmountPaid()
            .multiply(new BigDecimal(100))
            .divide(
                invoice.getInTaxTotal(),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP)
            .intValue());
  }

  public BigDecimal getMassPaymentAmount(List<Long> invoiceIdList, LocalDate date) {
    List<Invoice> invoiceList =
        invoiceRepo
            .all()
            .filter("self.id IN :invoiceIdList")
            .bind("invoiceIdList", invoiceIdList)
            .fetch();

    if (CollectionUtils.isNotEmpty(invoiceList) && date != null) {
      return invoiceList.stream()
          .map(Invoice::getInvoiceTermList)
          .flatMap(Collection::stream)
          .filter(invoiceTermService::isNotAwaitingPayment)
          .map(it -> invoiceTermService.getAmountRemaining(it, date, false))
          .reduce(BigDecimal::add)
          .orElse(BigDecimal.ZERO);
    } else {
      return BigDecimal.ZERO;
    }
  }

  public boolean applyFinancialDiscount(InvoicePayment invoicePayment) {
    LocalDate deadLineDate = invoicePayment.getFinancialDiscountDeadlineDate();
    return (invoicePayment != null
        && appAccountService.getAppAccount().getManageFinancialDiscount()
        && deadLineDate != null
        && deadLineDate.compareTo(invoicePayment.getPaymentDate()) >= 0);
  }

  public void computeFromInvoiceTermPayments(InvoicePayment invoicePayment) {
    BigDecimal newAmount = invoicePayment.getAmount();
    BigDecimal taxRate =
        invoicePayment.getFinancialDiscountTotalAmount().signum() == 0
            ? BigDecimal.ZERO
            : invoicePayment
                .getFinancialDiscountTaxAmount()
                .divide(invoicePayment.getFinancialDiscountTotalAmount(), 10, RoundingMode.HALF_UP);

    invoicePayment.setAmount(BigDecimal.ZERO);
    invoicePayment.setFinancialDiscountAmount(BigDecimal.ZERO);
    invoicePayment.setFinancialDiscountTaxAmount(BigDecimal.ZERO);
    invoicePayment.setFinancialDiscountTotalAmount(BigDecimal.ZERO);

    for (InvoiceTermPayment invoiceTermPayment : invoicePayment.getInvoiceTermPaymentList()) {
      invoicePayment.setAmount(invoicePayment.getAmount().add(invoiceTermPayment.getPaidAmount()));
      invoicePayment.setFinancialDiscountTotalAmount(
          invoicePayment
              .getFinancialDiscountTotalAmount()
              .add(invoiceTermPayment.getFinancialDiscountAmount()));
    }

    invoicePayment.setFinancialDiscountTotalAmount(
        invoicePayment
            .getFinancialDiscountTotalAmount()
            .multiply(newAmount)
            .divide(
                invoicePayment.getAmount(),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP));
    invoicePayment.setAmount(newAmount);
    invoicePayment.setFinancialDiscountTaxAmount(
        invoicePayment
            .getFinancialDiscountTotalAmount()
            .multiply(taxRate.divide(new BigDecimal(100)))
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP));
    invoicePayment.setFinancialDiscountAmount(
        invoicePayment
            .getFinancialDiscountTotalAmount()
            .subtract(invoicePayment.getFinancialDiscountTaxAmount()));
  }
}
