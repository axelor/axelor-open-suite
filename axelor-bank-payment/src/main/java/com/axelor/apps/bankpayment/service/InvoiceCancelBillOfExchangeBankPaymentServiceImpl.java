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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermReplaceService;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.bankpayment.report.ITranslation;
import com.axelor.apps.base.AxelorException;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class InvoiceCancelBillOfExchangeBankPaymentServiceImpl
    implements InvoiceCancelBillOfExchangeBankPaymentService {

  protected ReconcileService reconcileService;
  protected MoveReverseService moveReverseService;
  protected InvoiceRepository invoiceRepository;
  protected InvoiceTermReplaceService invoiceTermReplaceService;

  @Inject
  public InvoiceCancelBillOfExchangeBankPaymentServiceImpl(
      ReconcileService reconcileService,
      MoveReverseService moveReverseService,
      InvoiceRepository invoiceRepository,
      InvoiceTermReplaceService invoiceTermReplaceService) {
    this.reconcileService = reconcileService;
    this.moveReverseService = moveReverseService;
    this.invoiceRepository = invoiceRepository;
    this.invoiceTermReplaceService = invoiceTermReplaceService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelBillOfExchange(Invoice invoice) throws AxelorException {
    if (invoice == null
        || !invoice.getLcrAccounted()
        || invoice.getAmountRemaining().signum() == 0
        || invoice.getOldMove() == null) {
      return;
    }

    invoice = invoiceRepository.find(invoice.getId());

    Move billOfExchangeMove = invoice.getMove();
    Move oldMove = invoice.getOldMove();

    resetInvoiceBeforeBillOfExchangeCancellation(invoice, oldMove, billOfExchangeMove);

    Move reverseMove =
        moveReverseService.generateReverse(
            billOfExchangeMove, true, true, true, billOfExchangeMove.getDate());
    reverseMove.setDescription(
        String.format(
            I18n.get(ITranslation.BILL_OF_EXCHANGE_CANCELLATION), invoice.getInvoiceId()));
  }

  protected void resetInvoiceBeforeBillOfExchangeCancellation(
      Invoice invoice, Move oldMove, Move billOfExchangeMove) throws AxelorException {
    if (billOfExchangeMove == null || oldMove == null) {
      return;
    }

    List<InvoiceTerm> oldInvoiceTermList = new ArrayList<>();
    List<InvoiceTerm> billOfExchangeInvoiceTermList = new ArrayList<>();

    MoveLine billOfExchangeDebitMoveLine =
        billOfExchangeMove.getMoveLineList().stream()
            .filter(ml -> ml.getDebit().signum() != 0)
            .findFirst()
            .orElse(null);
    if (billOfExchangeDebitMoveLine == null) {
      return;
    }
    billOfExchangeInvoiceTermList.addAll(billOfExchangeDebitMoveLine.getInvoiceTermList());

    MoveLine debitMoveLine =
        oldMove.getMoveLineList().stream()
            .filter(ml -> ml.getDebit().signum() != 0)
            .findFirst()
            .orElse(null);
    if (debitMoveLine == null) {
      return;
    }
    oldInvoiceTermList.addAll(debitMoveLine.getInvoiceTermList());

    invoice.setLcrAccounted(false);
    invoice.setOldMove(null);
    invoice.setMove(oldMove);
    invoice.setPartnerAccount(debitMoveLine.getAccount());

    resetInvoiceTermAmounts(invoice, oldInvoiceTermList);
    invoiceTermReplaceService.replaceInvoiceTerms(
        invoice, oldInvoiceTermList, billOfExchangeInvoiceTermList);

    List<InvoiceTerm> invoiceTermListToReset =
        billOfExchangeMove.getMoveLineList().stream()
            .filter(ml -> ml.getCredit().signum() != 0)
            .findFirst()
            .map(MoveLine::getInvoiceTermList)
            .orElse(new ArrayList<>());
    if (!ObjectUtils.isEmpty(invoiceTermListToReset)) {
      resetInvoiceTermAmounts(invoice, invoiceTermListToReset);
    }
  }

  protected void resetInvoiceTermAmounts(Invoice invoice, List<InvoiceTerm> invoiceTermList) {
    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      if (!invoice.getInvoiceTermList().contains(invoiceTerm)) {
        invoiceTerm.setAmountRemaining(invoiceTerm.getAmount());
        invoiceTerm.setCompanyAmountRemaining(invoiceTerm.getCompanyAmount());
        invoiceTerm.setIsPaid(false);
      }
    }
  }
}
