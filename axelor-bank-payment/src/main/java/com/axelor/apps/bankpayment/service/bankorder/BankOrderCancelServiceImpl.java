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
package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionCancelService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentBankPaymentCancelService;
import com.axelor.apps.bankpayment.service.move.MoveCancelBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class BankOrderCancelServiceImpl implements BankOrderCancelService {

  protected InvoicePaymentRepository invoicePaymentRepository;
  protected BankOrderRepository bankOrderRepository;
  protected PaymentSessionRepository paymentSessionRepository;
  protected MoveRepository moveRepository;
  protected InvoicePaymentBankPaymentCancelService invoicePaymentBankPaymentCancelService;
  protected MoveCancelBankPaymentService moveCancelBankPaymentService;
  protected PaymentSessionCancelService paymentSessionCancelService;

  @Inject
  public BankOrderCancelServiceImpl(
      InvoicePaymentRepository invoicePaymentRepository,
      BankOrderRepository bankOrderRepository,
      PaymentSessionRepository paymentSessionRepository,
      MoveRepository moveRepository,
      InvoicePaymentBankPaymentCancelService invoicePaymentBankPaymentCancelService,
      MoveCancelBankPaymentService moveCancelBankPaymentService,
      PaymentSessionCancelService paymentSessionCancelService) {
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.bankOrderRepository = bankOrderRepository;
    this.paymentSessionRepository = paymentSessionRepository;
    this.moveRepository = moveRepository;
    this.invoicePaymentBankPaymentCancelService = invoicePaymentBankPaymentCancelService;
    this.moveCancelBankPaymentService = moveCancelBankPaymentService;
    this.paymentSessionCancelService = paymentSessionCancelService;
  }

  @Override
  public void cancelBankOrder(BankOrder bankOrder) throws AxelorException {
    bankOrder = this.cancelPayment(bankOrder);

    this.saveBankOrder(bankOrder);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void saveBankOrder(BankOrder bankOrder) {
    bankOrder.setStatusSelect(BankOrderRepository.STATUS_CANCELED);
    bankOrderRepository.save(bankOrder);
  }

  @Override
  public BankOrder cancelPayment(BankOrder bankOrder) throws AxelorException {
    this.cancelMoves(bankOrder);
    this.cancelInvoicePayments(bankOrder);

    return this.cancelPaymentSession(bankOrder);
  }

  protected void cancelInvoicePayments(BankOrder bankOrder) throws AxelorException {
    List<InvoicePayment> invoicePaymentList =
        invoicePaymentRepository.findByBankOrder(bankOrder).fetch();

    for (InvoicePayment invoicePayment : invoicePaymentList) {
      if (invoicePayment != null
          && invoicePayment.getStatusSelect() != InvoicePaymentRepository.STATUS_CANCELED) {
        invoicePaymentBankPaymentCancelService.cancelInvoicePayment(invoicePayment);
      }
    }
  }

  protected void cancelMoves(BankOrder bankOrder) throws AxelorException {
    Set<Move> moveSet = new HashSet<>();
    this.getBankOrderLinesMoves(bankOrder, moveSet);
    this.getInvoicePaymentMoves(bankOrder, moveSet);
    this.getPaymentSessionMoves(bankOrder, moveSet);
    if (CollectionUtils.isEmpty(moveSet)) {
      return;
    }
    for (Move move : moveSet) {
      moveCancelBankPaymentService.cancelGeneratedMove(moveRepository.find(move.getId()));
    }
  }

  protected void getBankOrderLinesMoves(BankOrder bankOrder, Set<Move> moveSet) {
    if (CollectionUtils.isEmpty(bankOrder.getBankOrderLineList())) {
      return;
    }

    for (BankOrderLine bankOrderLine : bankOrder.getBankOrderLineList()) {
      if (bankOrderLine.getSenderMove() != null) {
        moveSet.add(bankOrderLine.getSenderMove());
      }
      if (bankOrderLine.getReceiverMove() != null) {
        moveSet.add(bankOrderLine.getReceiverMove());
      }
    }
  }

  protected void getInvoicePaymentMoves(BankOrder bankOrder, Set<Move> moveSet) {
    for (InvoicePayment invoicePayment :
        invoicePaymentRepository.findByBankOrder(bankOrder).fetch()) {
      if (invoicePayment.getMove() != null) {
        moveSet.add(invoicePayment.getMove());
      }
    }
  }

  protected void getPaymentSessionMoves(BankOrder bankOrder, Set<Move> moveSet) {
    PaymentSession paymentSession = paymentSessionRepository.findByBankOrder(bankOrder);

    if (paymentSession != null) {
      for (Move move : moveRepository.findByPaymentSession(paymentSession).fetch()) {
        if (move != null) {
          moveSet.add(move);
        }
      }
    }
  }

  protected BankOrder cancelPaymentSession(BankOrder bankOrder) {
    PaymentSession paymentSession = paymentSessionRepository.findByBankOrder(bankOrder);

    if (paymentSession != null) {
      paymentSessionCancelService.cancelPaymentSession(paymentSession);
      return bankOrderRepository.find(bankOrder.getId());
    }

    return bankOrder;
  }
}
