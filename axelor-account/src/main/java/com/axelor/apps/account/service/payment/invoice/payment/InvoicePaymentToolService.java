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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InvoicePaymentToolService {

  public void updateAmountPaid(Invoice invoice) throws AxelorException;

  void updateHasPendingPayments(Invoice invoice);

  public void updatePaymentProgress(Invoice invoice);

  /**
   * @param company company from the invoice
   * @param invoicePayment
   * @return list of bankdetails in the payment mode for the given company.
   */
  public List<BankDetails> findCompatibleBankDetails(
      Company company, InvoicePayment invoicePayment);

  List<InvoicePayment> assignAdvancePayment(Invoice invoice, Invoice advancePayment);

  /**
   * Method to get move lines from payment. The move lines are either credit or debit depending on
   * the value of getCreditLine
   *
   * @param payments
   * @param getCreditLine
   * @return
   */
  List<MoveLine> getMoveLinesFromPayments(List<InvoicePayment> payments, boolean getCreditLine);

  void checkConditionBeforeSave(InvoicePayment invoicePayment) throws AxelorException;

  BigDecimal getPayableAmount(
      List<InvoiceTerm> invoiceTermList,
      LocalDate date,
      boolean manualChange,
      Currency paymentCurrency)
      throws AxelorException;

  void computeFinancialDiscount(InvoicePayment invoicePayment);

  BigDecimal getMassPaymentAmount(List<Long> invoiceIdList, LocalDate date);

  boolean applyFinancialDiscount(InvoicePayment invoicePayment);

  void computeFromInvoiceTermPayments(InvoicePayment invoicePayment);
}
