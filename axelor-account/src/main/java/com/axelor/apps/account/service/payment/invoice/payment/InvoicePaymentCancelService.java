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
package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.base.AxelorException;

public interface InvoicePaymentCancelService {

  /**
   * Method to cancel an invoice Payment
   *
   * <p>Cancel the eventual Move and Reconcile Compute the total amount paid on the linked invoice
   * Change the status to cancel
   *
   * @param invoicePayment An invoice payment
   * @throws AxelorException
   */
  public void cancel(InvoicePayment invoicePayment) throws AxelorException;

  public void updateCancelStatus(InvoicePayment invoicePayment) throws AxelorException;
}
