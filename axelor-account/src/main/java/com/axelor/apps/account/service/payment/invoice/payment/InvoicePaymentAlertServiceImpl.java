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

import com.axelor.apps.account.db.DepositSlip;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import java.util.Optional;

public class InvoicePaymentAlertServiceImpl implements InvoicePaymentAlertService {

  @Override
  public String validateBeforeReverse(InvoicePayment invoicePayment) {
    if (invoicePayment == null
        || invoicePayment.getStatusSelect() == InvoicePaymentRepository.STATUS_CANCELED) {
      return "";
    }

    String alert = AccountExceptionMessage.INVOICE_PAYMENT_ALERT_DEFAULT_REVERSE;

    DepositSlip depositSlip =
        Optional.of(invoicePayment)
            .map(InvoicePayment::getMove)
            .map(Move::getPaymentVoucher)
            .map(PaymentVoucher::getDepositSlip)
            .orElse(null);
    if (depositSlip != null && depositSlip.getPublicationDate() != null) {
      alert = AccountExceptionMessage.INVOICE_PAYMENT_ALERT_VOUCHER_DEPOSIT_REVERSE;
    }

    return alert;
  }
}
