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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceTermReplaceServiceImpl implements InvoiceTermReplaceService {
  protected ReconcileService reconcileService;
  protected InvoiceRepository invoiceRepo;
  protected InvoiceTermRepository invoiceTermRepo;

  @Inject
  public InvoiceTermReplaceServiceImpl(
      ReconcileService reconcileService,
      InvoiceRepository invoiceRepo,
      InvoiceTermRepository invoiceTermRepo) {
    this.reconcileService = reconcileService;
    this.invoiceRepo = invoiceRepo;
    this.invoiceTermRepo = invoiceTermRepo;
  }

  @Override
  public void replaceInvoiceTerms(
      Invoice invoice, Move move, List<MoveLine> invoiceMoveLineList, Account partnerAccount)
      throws AxelorException {
    List<InvoiceTerm> newInvoiceTermList = new ArrayList<>();
    List<InvoiceTerm> invoiceTermListToRemove = new ArrayList<>();
    copyInvoiceTerms(invoice, move, newInvoiceTermList, invoiceTermListToRemove);
    reconcilesMoves(move, invoiceMoveLineList, invoice, partnerAccount);
    replaceInvoiceTerms(invoice, newInvoiceTermList, invoiceTermListToRemove);
  }

  /**
   * Reconcile moves on account partnerAccount move line
   *
   * @param move
   * @param invoiceMoveLineList
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  protected void reconcilesMoves(
      Move move, List<MoveLine> invoiceMoveLineList, Invoice invoice, Account partnerAccount)
      throws AxelorException {
    Objects.requireNonNull(move);
    Objects.requireNonNull(invoiceMoveLineList);

    MoveLine creditMoveLine =
        move.getMoveLineList().stream()
            .filter(ml -> ml.getAccount().equals(partnerAccount))
            .findFirst()
            .orElseThrow(
                () ->
                    new AxelorException(
                        TraceBackRepository.CATEGORY_INCONSISTENCY,
                        "Missing move line with account %s",
                        partnerAccount.getName()));
    MoveLine debitMoveLine =
        invoiceMoveLineList.stream()
            .filter(ml -> ml.getAccount().equals(partnerAccount))
            .findFirst()
            .orElseThrow(
                () ->
                    new AxelorException(
                        TraceBackRepository.CATEGORY_INCONSISTENCY,
                        "Missing move line with account %s",
                        partnerAccount.getName()));

    Reconcile reconcile =
        reconcileService.createReconcile(
            debitMoveLine, creditMoveLine, creditMoveLine.getCredit(), false);
    reconcileService.confirmReconcile(reconcile, false, false);

    updateInvoiceTerms(creditMoveLine, debitMoveLine);
  }

  protected List<InvoiceTerm> splitInvoiceTerms(List<InvoiceTerm> invoiceTermList) {
    if (CollectionUtils.isEmpty(invoiceTermList)) {
      return new ArrayList<>();
    }

    List<InvoiceTerm> invoiceTermToRemove = new ArrayList<>();

    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      BigDecimal totalAmount =
          invoiceTerm.getInvoice() != null
              ? invoiceTerm.getInvoice().getCompanyInTaxTotal()
              : invoiceTerm.getMoveLine().getCurrencyAmount().abs();
      BigDecimal paidAmount = invoiceTerm.getAmount().subtract(invoiceTerm.getAmountRemaining());
      if (!invoiceTerm.getIsPaid() && paidAmount.signum() > 0 && totalAmount.signum() > 0) {
        BigDecimal amountRemaining = invoiceTerm.getAmountRemaining();
        BigDecimal newPercentage =
            paidAmount
                .multiply(new BigDecimal(100))
                .divide(
                    totalAmount, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
        InvoiceTerm newInvoiceTerm = invoiceTermRepo.copy(invoiceTerm, true);
        invoiceTerm.setAmount(paidAmount);
        invoiceTerm.setCompanyAmount(paidAmount);
        invoiceTerm.setAmountRemaining(BigDecimal.ZERO);
        invoiceTerm.setCompanyAmountRemaining(BigDecimal.ZERO);
        invoiceTerm.setPercentage(invoiceTerm.getPercentage().subtract(newPercentage));
        invoiceTerm.setIsPaid(true);
        newInvoiceTerm.setAmount(amountRemaining);
        newInvoiceTerm.setCompanyAmount(amountRemaining);
        newInvoiceTerm.setAmountRemaining(amountRemaining);
        newInvoiceTerm.setCompanyAmountRemaining(amountRemaining);
        newInvoiceTerm.setAmountPaid(BigDecimal.ZERO);
        newInvoiceTerm.setPercentage(newPercentage);
        MoveLine moveLine = invoiceTerm.getMoveLine();

        moveLine.addInvoiceTermListItem(newInvoiceTerm);
        invoiceTermToRemove.add(invoiceTerm);
      }
    }
    return invoiceTermToRemove;
  }

  protected void copyInvoiceTerms(
      Invoice invoice,
      Move move,
      List<InvoiceTerm> newInvoiceTermList,
      List<InvoiceTerm> invoiceTermListToRemove) {
    if (invoice == null || ObjectUtils.isEmpty(invoice.getInvoiceTermList())) {
      return;
    }

    MoveLine newDebitMoveLine =
        move.getMoveLineList().stream()
            .filter(dml -> dml.getDebit().signum() != 0)
            .findFirst()
            .orElse(null);
    if (newDebitMoveLine != null && !ObjectUtils.isEmpty(newDebitMoveLine.getInvoiceTermList())) {
      newInvoiceTermList.addAll(newDebitMoveLine.getInvoiceTermList());
    }

    List<InvoiceTerm> invoiceTermList = invoice.getInvoiceTermList();
    invoiceTermListToRemove.addAll(splitInvoiceTerms(invoiceTermList));

    invoiceTermList =
        invoiceTermList.stream()
            .filter(it -> !it.getIsPaid() && it.getAmountRemaining().signum() > 0)
            .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(invoiceTermList)) {
      invoiceTermListToRemove.addAll(invoiceTermList);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void replaceInvoiceTerms(
      Invoice invoice,
      List<InvoiceTerm> newInvoiceTermList,
      List<InvoiceTerm> invoiceTermListToRemove) {
    if (invoice == null
        || ObjectUtils.isEmpty(newInvoiceTermList)
        || ObjectUtils.isEmpty(invoiceTermListToRemove)) {
      return;
    }

    for (InvoiceTerm invoiceTerm : newInvoiceTermList) {
      invoice.addInvoiceTermListItem(invoiceTerm);
    }

    for (InvoiceTerm invoiceTerm : invoiceTermListToRemove) {
      invoice.removeInvoiceTermListItem(invoiceTerm);
      invoiceTerm.setInvoice(null);
    }

    replaceInvoiceTermsToRemoveWithCopy(invoiceTermListToRemove);
    invoiceRepo.save(invoice);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void replaceInvoiceTermsToRemoveWithCopy(List<InvoiceTerm> invoiceTermListToRemove) {
    for (InvoiceTerm invoiceTerm : invoiceTermListToRemove) {
      InvoiceTerm newInvoiceTerm = invoiceTermRepo.copy(invoiceTerm, true);
      invoiceTerm.setPaymentSession(null);
      MoveLine moveLine = invoiceTerm.getMoveLine();
      moveLine.addInvoiceTermListItem(newInvoiceTerm);
      moveLine.removeInvoiceTermListItem(invoiceTerm);
      invoiceTerm.setMoveLine(null);
      invoiceTermRepo.remove(invoiceTerm);
    }
  }

  protected void updateInvoiceTerms(MoveLine creditMoveLine, MoveLine debitMoveLine) {
    updateAmounts(creditMoveLine.getInvoiceTermList());
    updateAmounts(debitMoveLine.getInvoiceTermList());
  }

  protected void updateAmounts(List<InvoiceTerm> invoiceTermList) {
    if (!ObjectUtils.isEmpty(invoiceTermList)) {
      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        MoveLine moveLine = invoiceTerm.getMoveLine();
        moveLine.setAmountRemaining(
            moveLine.getAmountRemaining().subtract(invoiceTerm.getAmountRemaining()));
        invoiceTerm.setAmountRemaining(BigDecimal.ZERO);
        invoiceTerm.setCompanyAmountRemaining(BigDecimal.ZERO);
        invoiceTerm.setIsPaid(true);
      }
    }
  }
}
