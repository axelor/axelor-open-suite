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
package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.base.AxelorException;
import java.time.LocalDate;
import java.util.Collection;

public interface BankOrderMergeService {

  public BankOrder mergeBankOrders(Collection<BankOrder> bankOrders) throws AxelorException;

  /**
   * Merge bank orders from invoice payments.
   *
   * @param invoicePayments
   * @return
   * @throws AxelorException
   */
  BankOrder mergeFromInvoicePayments(Collection<InvoicePayment> invoicePayments)
      throws AxelorException;

  BankOrder mergeFromInvoicePayments(
      Collection<InvoicePayment> invoicePayments, LocalDate bankOrderDate) throws AxelorException;
}
