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
package com.axelor.apps.bankpayment.service.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentAlertServiceImpl;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import java.util.Optional;

public class InvoicePaymentAlertServiceBankPayImpl extends InvoicePaymentAlertServiceImpl {

  @Override
  public String validateBeforeReverse(InvoicePayment invoicePayment) {
    String alert = super.validateBeforeReverse(invoicePayment);

    if (BankOrderRepository.STATUS_CARRIED_OUT
        == Optional.ofNullable(invoicePayment)
            .map(InvoicePayment::getBankOrder)
            .map(BankOrder::getStatusSelect)
            .orElse(BankOrderRepository.STATUS_DRAFT)) {
      alert = BankPaymentExceptionMessage.INVOICE_PAYMENT_ALERT_BANK_ORDER_REVERSE;
    }

    return alert;
  }
}
