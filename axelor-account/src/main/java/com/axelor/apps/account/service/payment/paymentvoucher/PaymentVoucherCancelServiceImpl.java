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
package com.axelor.apps.account.service.payment.paymentvoucher;

import com.axelor.apps.account.db.DepositSlip;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentVoucherCancelServiceImpl implements PaymentVoucherCancelService {

  protected PaymentVoucherRepository paymentVoucherRepository;
  protected MoveRepository moveRepository;
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected InvoicePaymentCancelService invoicePaymentCancelService;

  @Inject
  public PaymentVoucherCancelServiceImpl(
      PaymentVoucherRepository paymentVoucherRepository,
      MoveRepository moveRepository,
      InvoicePaymentRepository invoicePaymentRepository,
      InvoicePaymentCancelService invoicePaymentCancelService) {
    this.paymentVoucherRepository = paymentVoucherRepository;
    this.moveRepository = moveRepository;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.invoicePaymentCancelService = invoicePaymentCancelService;
  }

  @Override
  @Transactional
  public PaymentVoucher cancelPaymentVoucher(PaymentVoucher paymentVoucher) {
    paymentVoucher.setStatusSelect(PaymentVoucherRepository.STATUS_CANCELED);
    return paymentVoucher;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelPaymentVoucherManually(PaymentVoucher paymentVoucher, CancelReason cancelReason)
      throws AxelorException {
    checkGeneratedMoveIsReversed(paymentVoucher);
    checkAndHandleDepositSlip(paymentVoucher);
    handleInvoicePayments(paymentVoucher);

    paymentVoucher.setStatusSelect(PaymentVoucherRepository.STATUS_CANCELED);
    paymentVoucher.setCancellationReason(cancelReason);
    paymentVoucher.setCancellationDate(LocalDateTime.now());
    paymentVoucher.setCancelledByUser(AuthUtils.getUser());
    paymentVoucherRepository.save(paymentVoucher);
  }

  protected void checkGeneratedMoveIsReversed(PaymentVoucher paymentVoucher)
      throws AxelorException {
    Move generatedMove = paymentVoucher.getGeneratedMove();
    if (generatedMove == null || generatedMove.getOrigin() == null) {
      throw new AxelorException(
          paymentVoucher,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.PAYMENT_VOUCHER_CANCEL_NO_MOVE));
    }
    long reverseCount =
        moveRepository
            .all()
            .filter(
                "self.origin = :origin "
                    + "AND self.technicalOriginSelect = :automatic "
                    + "AND self.id != :moveId "
                    + "AND self.company = :company")
            .bind("origin", generatedMove.getOrigin())
            .bind("automatic", MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC)
            .bind("moveId", generatedMove.getId())
            .bind("company", generatedMove.getCompany())
            .count();
    if (reverseCount == 0) {
      throw new AxelorException(
          paymentVoucher,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.PAYMENT_VOUCHER_CANCEL_MOVE_NOT_REVERSED));
    }
  }

  protected void checkAndHandleDepositSlip(PaymentVoucher paymentVoucher) throws AxelorException {
    DepositSlip depositSlip = paymentVoucher.getDepositSlip();
    if (depositSlip == null) {
      return;
    }
    if (depositSlip.getPublicationDate() != null) {
      throw new AxelorException(
          paymentVoucher,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.PAYMENT_VOUCHER_CANCEL_DEPOSIT_SLIP_PUBLISHED),
          depositSlip.getDepositNumber());
    }
    paymentVoucher.setDepositSlip(null);
  }

  protected void handleInvoicePayments(PaymentVoucher paymentVoucher) throws AxelorException {
    Move generatedMove = paymentVoucher.getGeneratedMove();
    List<InvoicePayment> invoicePayments =
        invoicePaymentRepository.findByMove(generatedMove).fetch();
    for (InvoicePayment ip : invoicePayments) {
      if (ip.getStatusSelect() != InvoicePaymentRepository.STATUS_CANCELED) {
        invoicePaymentCancelService.cancel(ip);
      }
    }
  }
}
