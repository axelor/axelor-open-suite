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
import com.axelor.apps.account.service.invoice.InvoiceTermFilterService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
      InvoiceRepository invoiceRepository) {
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
    if (invoice != null
        && otherMove.getFunctionalOriginSelect()
            != MoveRepository.FUNCTIONAL_ORIGIN_DOUBTFUL_CUSTOMER) {
      if (otherMove.getFunctionalOriginSelect() != MoveRepository.FUNCTIONAL_ORIGIN_IRRECOVERABLE) {
        invoicePayment = invoicePaymentRepository.findByReconcileAndInvoice(reconcile, invoice);

        if (invoicePayment == null) {
          invoicePayment = this.getExistingInvoicePayment(invoice, otherMove);
        }
      }

      if (invoicePayment == null
          && moveLine.getAccount().getUseForPartnerBalance()
          && otherMoveLine.getAccount().getUseForPartnerBalance()) {

        BigDecimal invoicePaymentAmount = amount;
        if (!reconcileCheckService.isCompanyCurrency(reconcile, null, otherMove)) {
          invoicePaymentAmount = this.getTotal(moveLine, otherMoveLine, amount, false);
        }

        invoicePayment =
            invoicePaymentCreateService.createInvoicePayment(
                invoice, invoicePaymentAmount, otherMove);
        invoicePayment.setReconcile(reconcile);
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
    }

    if (invoicePayment != null) {
      invoicePaymentToolService.updateAmountPaid(invoicePayment.getInvoice());
      invoicePaymentRepository.save(invoicePayment);
    } else if (!ObjectUtils.isEmpty(invoiceTermPaymentList)) {
      invoiceTermPaymentList.forEach(it -> invoiceTermPaymentRepository.save(it));
    }
  }

  protected InvoicePayment getExistingInvoicePayment(Invoice invoice, Move move) {
    return invoice.getInvoicePaymentList().stream()
        .filter(
            it -> (it.getMove() != null && it.getMove().equals(move) && it.getReconcile() == null))
        .findFirst()
        .orElse(null);
  }

  protected Map<InvoiceTerm, Integer> getInvoiceTermPfpStatus(Invoice invoice) {
    Map<InvoiceTerm, Integer> map = new HashMap<>();

    if (invoice != null && CollectionUtils.isNotEmpty(invoice.getInvoiceTermList())) {
      List<InvoiceTerm> invoiceTermList = invoice.getInvoiceTermList();

      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        if (invoiceTerm.getPfpValidateStatusSelect()
            != InvoiceTermRepository.PFP_STATUS_LITIGATION) {
          invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_VALIDATED);
        }
        map.put(invoiceTerm, invoiceTerm.getPfpValidateStatusSelect());
      }
    }

    return map;
  }

  protected BigDecimal getTotal(
      MoveLine moveLine, MoveLine otherMoveLine, BigDecimal amount, boolean isInvoicePayment) {
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
          computePaidRatio(moveLineAmount, amount, invoiceAmount, computedAmount, isInvoicePayment)
              .multiply(moveLine.getCurrencyAmount().abs());
    }

    total = currencyScaleService.getScaledValue(moveLine, total);

    if (amount.compareTo(otherMoveLine.getCredit().max(otherMoveLine.getDebit())) == 0
        && total.compareTo(otherMoveLine.getCurrencyAmount()) != 0) {
      total = otherMoveLine.getCurrencyAmount().abs();
    }

    return total;
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
    if (move != null
        && move.getPaymentVoucher() != null
        && CollectionUtils.isNotEmpty(move.getPaymentVoucher().getPayVoucherElementToPayList())) {
      return move.getPaymentVoucher().getPayVoucherElementToPayList().stream()
          .filter(it -> it.getMoveLine().equals(moveLine) && !it.getInvoiceTerm().getIsPaid())
          .sorted(Comparator.comparing(PayVoucherElementToPay::getSequence))
          .map(PayVoucherElementToPay::getInvoiceTerm)
          .collect(Collectors.toList());
    } else {
      List<InvoiceTerm> invoiceTermsToPay;
      if (invoice != null && CollectionUtils.isNotEmpty(invoice.getInvoiceTermList())) {
        invoiceTermsToPay =
            invoiceTermFilterService.getUnpaidInvoiceTermsFilteredWithoutPfpCheck(invoice);

      } else if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
        invoiceTermsToPay = this.getInvoiceTermsFromMoveLine(moveLine.getInvoiceTermList());

      } else {
        return null;
      }
      if (CollectionUtils.isNotEmpty(invoiceTermsToPay)
          && move != null
          && move.getPaymentSession() != null) {
        return invoiceTermsToPay.stream()
            .filter(
                it ->
                    it.getPaymentSession() != null
                        && it.getPaymentSession().equals(move.getPaymentSession()))
            .collect(Collectors.toList());
      } else {
        return invoiceTermsToPay;
      }
    }
  }

  protected List<InvoiceTerm> getInvoiceTermsFromMoveLine(List<InvoiceTerm> invoiceTermList) {
    return invoiceTermList.stream()
        .filter(it -> !it.getIsPaid())
        .sorted(this::compareInvoiceTerm)
        .collect(Collectors.toList());
  }

  protected int compareInvoiceTerm(InvoiceTerm invoiceTerm1, InvoiceTerm invoiceTerm2) {
    LocalDate date1, date2;

    if (invoiceTerm1.getEstimatedPaymentDate() != null
        && invoiceTerm2.getEstimatedPaymentDate() != null) {
      date1 = invoiceTerm1.getEstimatedPaymentDate();
      date2 = invoiceTerm2.getEstimatedPaymentDate();
    } else {
      date1 = invoiceTerm1.getDueDate();
      date2 = invoiceTerm2.getDueDate();
    }

    return date1.compareTo(date2);
  }
}
