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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface PaymentSessionBankOrderService {
  BankOrder generateBankOrderFromPaymentSession(PaymentSession paymentSession)
      throws AxelorException;

  void createOrUpdateBankOrderLineFromInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      BankOrder bankOrder,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList)
      throws AxelorException;

  void manageInvoicePayment(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm, BigDecimal reconciledAmount);
}
