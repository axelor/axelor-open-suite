/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.util.List;

public interface InvoicePaymentToolService {

  @Transactional(rollbackOn = {Exception.class})
  public void updateAmountPaid(Invoice invoice) throws AxelorException;

  void updateHasPendingPayments(Invoice invoice);

  /**
   * @param company company from the invoice
   * @param invoicePayment
   * @return list of bankdetails in the payment mode for the given company.
   */
  public List<BankDetails> findCompatibleBankDetails(
      Company company, InvoicePayment invoicePayment);

  List<InvoicePayment> assignAdvancePayment(Invoice invoice, Invoice advancePayment);

  List<MoveLine> getCreditMoveLinesFromPayments(List<InvoicePayment> payments);
}
