/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PayVoucherElementToPay;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermPaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermFilterService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpToolService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ReconcileInvoiceTermComputationServiceImpl
    implements ReconcileInvoiceTermComputationService {

  protected ReconcileCheckService reconcileCheckService;
  protected CurrencyScaleService currencyScaleService;
  protected InvoiceTermFilterService invoiceTermFilterService;
  protected InvoicePaymentCreateService invoicePaymentCreateService;
  protected InvoiceTermService invoiceTermService;
  protected InvoicePaymentToolService invoicePaymentToolService;
  protected CurrencyService currencyService;
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected InvoiceTermPaymentRepository invoiceTermPaymentRepository;
  protected InvoiceRepository invoiceRepository;
  protected InvoiceTermPfpService invoiceTermPfpService;
  protected PfpService pfpService;
  protected InvoiceTermPfpToolService invoiceTermPfpToolService;

  @Inject
  public ReconcileInvoiceTermComputationServiceImpl(
      ReconcileCheckService reconcileCheckService,
      CurrencyScaleService currencyScaleService,
      InvoiceTermFilterService invoiceTermFilterService,
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoiceTermService invoiceTermService,
      InvoicePaymentToolService invoicePaymentToolService,
      CurrencyService currencyService,
      InvoicePaymentRepository invoicePaymentRepository,
      InvoiceTermPaymentRepository invoiceTermPaymentRepository,
      InvoiceRepository invoiceRepository,
      InvoiceTermPfpService invoiceTermPfpService,
      PfpService pfpService,
      InvoiceTermPfpToolService invoiceTermPfpToolService) {
    this.reconcileCheckService = reconcileCheckService;
    this.currencyScaleService = currencyScaleService;
    this.invoiceTermFilterService = invoiceTermFilterService;
    this.invoicePaymentCreateService = invoicePaymentCreateService;
    this.invoiceTermService = invoiceTermService;
    this.invoicePaymentToolService = invoicePaymentToolService;
    this.currencyService = currencyService;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.invoiceTermPaymentRepository = invoiceTermPaymentRepository;
    this.invoiceRepository = invoiceRepository;
    this.invoiceTermPfpService = invoiceTermPfpService;
    this.pfpService = pfpService;
    this.invoiceTermPfpToolService = invoiceTermPfpToolService;
  }

  @Override
  public void updatePayments(Reconcile reconcile, boolean updateInvoiceTerms)
      throws AxelorException {

    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();
    Move debitMove = debitMoveLine.getMove();
    Move creditMove = creditMoveLine.getMove();
    Invoice debitInvoice = invoiceRepository.findByMove(debitMove);
    if (debitInvoice == null) {
      debitInvoice = invoiceRepository.findByOldMove(debitMove);
      debitInvoice =
          debitInvoice != null ? (debitInvoice.getLcrAccounted() ? debitInvoice : null) : null;
    }
    Invoice creditInvoice = invoiceRepository.findByMove(creditMove);
    if (creditInvoice == null) {
      creditInvoice = invoiceRepository.findByOldMove(creditMove);
      creditInvoice =
          creditInvoice != null ? (creditInvoice.getLcrAccounted() ? creditInvoice : null) : null;
    }
    BigDecimal amount = reconcile.getAmount();

    reconcileCheckService.checkCurrencies(debitMoveLine, creditMoveLine);

    if (reconcile.getForeignExchangeMove() != null
        && creditInvoice == null
        && debitInvoice == null) {
      creditInvoice = creditMove.getInvoice() != null ? creditMove.getInvoice() : null;
      debitInvoice = debitMove.getInvoice() != null ? debitMove.getInvoice() : null;
    }

    this.updatePayment(
        reconcile,
        debitMoveLine,
        creditMoveLine,
        debitInvoice,
        debitMove,
        creditMove,
        amount,
        updateInvoiceTerms);
    this.updatePayment(
        reconcile,
        creditMoveLine,
        debitMoveLine,
        creditInvoice,
        creditMove,
        debitMove,
        amount,
        updateInvoiceTerms);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updatePayment(
      Reconcile reconcile,
      MoveLine moveLine,
      MoveLine otherMoveLine,
      Invoice invoice,
      Move move,
      Move otherMove,
      BigDecimal amount,
      boolean updateInvoiceTerms)
      throws AxelorException {
    InvoicePayment invoicePayment = null;
    boolean refreshInvoiceAmounts = false;
    if (invoice != null
        && otherMove.getFunctionalOriginSelect()
            != MoveRepository.FUNCTIONAL_ORIGIN_DOUBTFUL_CUSTOMER) {
      if (otherMove.getFunctionalOriginSelect() != MoveRepository.FUNCTIONAL_ORIGIN_IRRECOVERABLE) {
        invoicePayment = invoicePaymentRepository.findByReconcileAndInvoice(reconcile, invoice);

        if (invoicePayment == null) {
          invoicePayment = this.getExistingInvoicePayment(invoice, otherMove);
        }
      }

      if (moveLine.getAccount().getUseForPartnerBalance()
          && otherMoveLine.getAccount().getUseForPartnerBalance()) {

        BigDecimal invoicePaymentAmount = amount;
        boolean isCompanyCurrency =
            reconcileCheckService.isCompanyCurrency(reconcile, null, otherMove);
        if (!isCompanyCurrency) {
          invoicePaymentAmount = this.getTotal(moveLine, otherMoveLine, amount, true);
        }

        InvoicePayment foreignExchangePayment =
            invoicePaymentRepository.findByMove(reconcile.getForeignExchangeMove()).fetchOne();
        if (foreignExchangePayment == null
            && reconcile.getForeignExchangeMove() != null
            && (invoicePayment == null || !reconcile.equals(invoicePayment.getReconcile()))) {
          invoicePayment =
              invoicePaymentCreateService.createInvoicePayment(
                  invoice, invoicePaymentAmount, reconcile.getForeignExchangeMove());
          invoicePayment.setReconcile(reconcile);
        } else if (invoicePayment == null) {
          // A residual left in company currency only (e.g. balanced by an adjustment move) must
          // not be counted as a payment of an invoice already fully paid in its own currency
          if (isCompanyCurrency
              || !this.isMoveLineFullyPaidInCurrency(reconcile, moveLine, move, otherMove)) {
            invoicePayment =
                invoicePaymentCreateService.createInvoicePayment(
                    invoice, invoicePaymentAmount, otherMove);
            invoicePayment.setReconcile(reconcile);
          } else {
            refreshInvoiceAmounts = true;
          }
        }
      }

      if (!reconcileCheckService.isCompanyCurrency(reconcile, invoicePayment, otherMove)) {
        amount = this.getTotal(moveLine, otherMoveLine, amount, false);
      }
    } else if (!reconcileCheckService.isCompanyCurrency(reconcile, invoicePayment, otherMove)) {
      amount = this.getTotal(moveLine, otherMoveLine, amount, false);
    } else {
      amount =
          currencyService.getAmountCurrencyConvertedAtDate(
              otherMove.getCurrency(), move.getCurrency(), amount, move.getDate());
    }

    List<InvoiceTermPayment> invoiceTermPaymentList = null;
    if (moveLine.getAccount().getUseForPartnerBalance() && updateInvoiceTerms) {
      List<InvoiceTerm> invoiceTermList = this.getInvoiceTermsToPay(invoice, otherMove, moveLine);
      Map<InvoiceTerm, Integer> invoiceTermPfpValidateStatusSelectMap =
          this.getInvoiceTermPfpStatus(invoice);

      if (invoicePayment != null) {
        amount =
            currencyService.getAmountCurrencyConvertedAtDate(
                invoicePayment.getCurrency(),
                invoicePayment.getCompanyCurrency(),
                invoicePayment.getAmount(),
                invoicePayment.getPaymentDate());
      }

      invoiceTermPaymentList =
          invoiceTermService.updateInvoiceTerms(
              invoiceTermList,
              invoicePayment,
              amount,
              reconcile,
              invoiceTermPfpValidateStatusSelectMap);

      invoiceTermPfpValidateStatusSelectMap
          .keySet()
          .forEach(
              it -> it.setPfpValidateStatusSelect(invoiceTermPfpValidateStatusSelectMap.get(it)));
      invoiceTermPfpService.refreshInvoicePfpStatus(invoice);
    }

    if (invoicePayment != null) {
      invoicePaymentToolService.updateAmountPaid(invoicePayment.getInvoice());
      invoicePaymentRepository.save(invoicePayment);
    } else {
      if (!ObjectUtils.isEmpty(invoiceTermPaymentList)) {
        invoiceTermPaymentList.forEach(it -> invoiceTermPaymentRepository.save(it));
      }
      if (refreshInvoiceAmounts) {
        invoicePaymentToolService.updateAmountPaid(invoice);
      }
    }
  }

  /**
   * Check whether the move line is already fully settled in its own currency by its other confirmed
   * reconciles. Invoice terms cannot be used here, as a reconcile balancing a company currency
   * residual is confirmed before the invoice terms of the reconcile that left it are updated.
   */
  protected boolean isMoveLineFullyPaidInCurrency(
      Reconcile reconcile, MoveLine moveLine, Move move, Move otherMove) {
    BigDecimal currencyAmount = moveLine.getCurrencyAmount().abs();
    Currency currency = move.getCurrency();
    if (currencyAmount.signum() == 0
        || currency == null
        || !currency.equals(otherMove.getCurrency())) {
      return false;
    }

    BigDecimal currencyAmountPaid = BigDecimal.ZERO;
    for (Reconcile otherReconcile : this.getConfirmedReconcileList(moveLine)) {
      if (otherReconcile.equals(reconcile)) {
        continue;
      }
      MoveLine reconciledMoveLine = this.getOtherMoveLine(otherReconcile, moveLine);

      // Foreign exchange gap moves are in company currency and do not settle the move currency
      if (!currency.equals(reconciledMoveLine.getMove().getCurrency())) {
        continue;
      }

      currencyAmountPaid =
          currencyAmountPaid.add(
              this.getReconciledCurrencyAmount(otherReconcile, reconciledMoveLine, currency));
    }

    return currencyAmountPaid.compareTo(currencyAmount) >= 0;
  }

  /**
   * Share of the reconciled move line currency amount settled by the reconcile. The part of the
   * line reconciled with foreign exchange gap moves is left out, as it has no currency amount.
   */
  protected BigDecimal getReconciledCurrencyAmount(
      Reconcile reconcile, MoveLine reconciledMoveLine, Currency currency) {
    BigDecimal lineAmount = reconciledMoveLine.getDebit().add(reconciledMoveLine.getCredit());
    for (Reconcile lineReconcile : this.getConfirmedReconcileList(reconciledMoveLine)) {
      if (!currency.equals(
          this.getOtherMoveLine(lineReconcile, reconciledMoveLine).getMove().getCurrency())) {
        lineAmount = lineAmount.subtract(lineReconcile.getAmount());
      }
    }

    if (lineAmount.signum() <= 0) {
      return BigDecimal.ZERO;
    }

    return reconciledMoveLine
        .getCurrencyAmount()
        .abs()
        .multiply(reconcile.getAmount())
        .divide(
            lineAmount, currencyScaleService.getScale(reconciledMoveLine), RoundingMode.HALF_UP);
  }

  protected List<Reconcile> getConfirmedReconcileList(MoveLine moveLine) {
    return Beans.get(ReconcileRepository.class)
        .all()
        .filter(
            "(self.debitMoveLine = :moveLine OR self.creditMoveLine = :moveLine)"
                + " AND self.statusSelect = :statusConfirmed")
        .bind("moveLine", moveLine)
        .bind("statusConfirmed", ReconcileRepository.STATUS_CONFIRMED)
        .fetch();
  }

  protected MoveLine getOtherMoveLine(Reconcile reconcile, MoveLine moveLine) {
    return moveLine.equals(reconcile.getDebitMoveLine())
        ? reconcile.getCreditMoveLine()
        : reconcile.getDebitMoveLine();
  }

  protected InvoicePayment getExistingInvoicePayment(Invoice invoice, Move move) {
    return invoice.getInvoicePaymentList().stream()
        .filter(
            it -> (it.getMove() != null && it.getMove().equals(move) && it.getReconcile() == null))
        .findFirst()
        .orElse(null);
  }

  protected Map<InvoiceTerm, Integer> getInvoiceTermPfpStatus(Invoice invoice)
      throws AxelorException {
    Map<InvoiceTerm, Integer> map = new HashMap<>();

    if (invoice == null || CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
      return map;
    }

    boolean shouldAutoValidate =
        pfpService.getPfpCondition(invoice)
            && invoice.getOperationTypeSelect()
                == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE;

    List<InvoiceTerm> invoiceTermList = invoice.getInvoiceTermList();

    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      int status = invoiceTerm.getPfpValidateStatusSelect();
      if (shouldAutoValidate
          && status == InvoiceTermRepository.PFP_STATUS_AWAITING
          && invoiceTerm.getAmountRemaining().signum() == 0) {
        status = InvoiceTermRepository.PFP_STATUS_VALIDATED;
      }
      map.put(invoiceTerm, status);
    }

    return map;
  }

  protected BigDecimal getTotal(
      MoveLine moveLine, MoveLine otherMoveLine, BigDecimal amount, boolean isForInvoicePayment) {
    BigDecimal total;
    BigDecimal moveLineAmount = moveLine.getCredit().add(moveLine.getDebit());
    BigDecimal rate = moveLine.getCurrencyRate();
    BigDecimal invoiceAmount =
        moveLine.getAmountPaid().add(moveLineAmount.subtract(moveLine.getAmountPaid()));
    BigDecimal computedAmount =
        moveLineAmount
            .divide(rate, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP)
            .multiply(rate);

    // Recompute currency rate to avoid rounding issue
    total = amount.divide(rate, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    if (total.stripTrailingZeros().scale() > currencyScaleService.getScale(otherMoveLine)) {
      total =
          computePaidRatio(
                  moveLineAmount, amount, invoiceAmount, computedAmount, isForInvoicePayment)
              .multiply(moveLine.getCurrencyAmount().abs());
    }

    total = currencyScaleService.getScaledValue(moveLine, total);

    if (amount.compareTo(otherMoveLine.getCredit().max(otherMoveLine.getDebit())) == 0
        && total.compareTo(otherMoveLine.getCurrencyAmount()) != 0) {
      total = otherMoveLine.getCurrencyAmount().abs();
    }

    if (isForInvoicePayment) {
      return total;
    } else {
      if (rate.compareTo(otherMoveLine.getCurrencyRate()) > 0) {
        return amount;
      } else {
        return currencyScaleService.getCompanyScaledValue(
            moveLine, total.multiply(otherMoveLine.getCurrencyRate()));
      }
    }
  }

  protected BigDecimal computePaidRatio(
      BigDecimal moveLineAmount,
      BigDecimal amountToPay,
      BigDecimal invoiceAmount,
      BigDecimal computedAmount,
      boolean isInvoicePayment) {
    BigDecimal ratioPaid = BigDecimal.ONE;
    int percentageScale = AppBaseService.DEFAULT_NB_DECIMAL_DIGITS;
    BigDecimal percentage =
        amountToPay.divide(computedAmount, percentageScale, RoundingMode.HALF_UP);

    if (isInvoicePayment) {
      // ReCompute percentage paid when it's partial payment with invoice payment
      percentage =
          amountToPay.divide(
              invoiceAmount, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    } else if (moveLineAmount
            .multiply(percentage)
            .setScale(percentageScale, RoundingMode.HALF_UP)
            .compareTo(amountToPay)
        != 0) {
      // Compute ratio paid when it's invoice term partial payment
      if (amountToPay.compareTo(invoiceAmount) != 0) {
        percentage = invoiceAmount.divide(computedAmount, percentageScale, RoundingMode.HALF_UP);
      } else {
        percentage =
            invoiceAmount.divide(
                computedAmount, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
      }
      ratioPaid =
          amountToPay.divide(
              invoiceAmount, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    }

    return ratioPaid.multiply(percentage);
  }

  protected List<InvoiceTerm> getInvoiceTermsToPay(Invoice invoice, Move move, MoveLine moveLine)
      throws AxelorException {
    List<InvoiceTerm> voucherTerms = getInvoiceTermsFromPaymentVoucher(move, moveLine);
    if (CollectionUtils.isNotEmpty(voucherTerms)) {
      return filterInvoiceTermsByPfp(invoice, move, voucherTerms);
    }

    List<InvoiceTerm> invoiceTermsToPay = getUnpaidInvoiceTerms(invoice, moveLine);
    invoiceTermsToPay = filterInvoiceTermsByPaymentSession(move, invoiceTermsToPay);
    return filterInvoiceTermsByPfp(invoice, move, invoiceTermsToPay);
  }

  protected List<InvoiceTerm> getInvoiceTermsFromPaymentVoucher(Move move, MoveLine moveLine) {
    if (move == null
        || move.getPaymentVoucher() == null
        || CollectionUtils.isEmpty(move.getPaymentVoucher().getPayVoucherElementToPayList())) {
      return Collections.emptyList();
    }

    return move.getPaymentVoucher().getPayVoucherElementToPayList().stream()
        .filter(it -> it.getMoveLine().equals(moveLine) && !it.getInvoiceTerm().getIsPaid())
        .sorted(Comparator.comparing(PayVoucherElementToPay::getSequence))
        .map(PayVoucherElementToPay::getInvoiceTerm)
        .collect(Collectors.toList());
  }

  protected List<InvoiceTerm> getUnpaidInvoiceTerms(Invoice invoice, MoveLine moveLine)
      throws AxelorException {
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      return invoiceTermService.getInvoiceTermsFromMoveLine(moveLine.getInvoiceTermList());
    }
    if (invoice != null && CollectionUtils.isNotEmpty(invoice.getInvoiceTermList())) {
      return invoiceTermFilterService.getUnpaidInvoiceTermsFilteredWithoutPfpCheck(invoice);
    }
    return null;
  }

  protected List<InvoiceTerm> filterInvoiceTermsByPfp(
      Invoice invoice, Move move, List<InvoiceTerm> invoiceTerms) throws AxelorException {
    if (CollectionUtils.isEmpty(invoiceTerms) || !isPfpCheckApplicable(invoice, move)) {
      return invoiceTerms;
    }

    List<Integer> validatedStatuses = invoiceTermPfpToolService.getAlreadyValidatedStatusList();
    return invoiceTerms.stream()
        .filter(it -> validatedStatuses.contains(it.getPfpValidateStatusSelect()))
        .collect(Collectors.toList());
  }

  protected List<InvoiceTerm> filterInvoiceTermsByPaymentSession(
      Move move, List<InvoiceTerm> invoiceTerms) {
    if (CollectionUtils.isEmpty(invoiceTerms)
        || move == null
        || move.getPaymentSession() == null
        || invoiceTerms.stream().noneMatch(it -> it.getPaymentSession() != null)) {
      return invoiceTerms;
    }

    return invoiceTerms.stream()
        .filter(it -> move.getPaymentSession().equals(it.getPaymentSession()))
        .collect(Collectors.toList());
  }

  protected boolean isPfpCheckApplicable(Invoice invoice, Move move) throws AxelorException {
    return invoice != null
        && invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
        && pfpService.getPfpCondition(invoice)
        && (move == null
            || move.getInvoice() == null
            || !InvoiceToolService.isRefund(move.getInvoice()));
  }
}
