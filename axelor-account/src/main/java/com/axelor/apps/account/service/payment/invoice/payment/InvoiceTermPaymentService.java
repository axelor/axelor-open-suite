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
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.util.List;

public interface InvoiceTermPaymentService {

  /**
   * Method to init invoiceTermPayments based on invoiceTerm to pay
   *
   * @param invoicePayment
   * @param invoiceTermsToPay
   * @return
   */
  public InvoicePayment initInvoiceTermPayments(
      InvoicePayment invoicePayment, List<InvoiceTerm> invoiceTermsToPay);

  /**
   * Method to init invoiceTermPayments based on invoiceTerm to pay and inserted amount
   *
   * @param invoicePayment
   * @param invoiceTermsToPay
   * @param availableAmount
   * @return
   */
  public List<InvoiceTermPayment> initInvoiceTermPaymentsWithAmount(
      InvoicePayment invoicePayment,
      List<InvoiceTerm> invoiceTermsToPay,
      BigDecimal availableAmount,
      BigDecimal reconcileAmount);

  /**
   * Method to create invoiceTermPayments for an invoicePayment
   *
   * @param invoicePayment
   * @return
   */
  public void createInvoicePaymentTerms(
      InvoicePayment invoicePayment, List<InvoiceTerm> invoiceTermToPayList) throws AxelorException;

  /**
   * Method to create new InvoiceTermPayment usign invoiceTerm amountRemaining
   *
   * @param invoicePayment
   * @param invoiceTermToPay
   * @param paidAmount
   * @return
   */
  public InvoiceTermPayment createInvoiceTermPayment(
      InvoicePayment invoicePayment, InvoiceTerm invoiceTermToPay, BigDecimal paidAmount);

  /**
   * Method to compute total paid amount of invoiceTermPayments
   *
   * @param invoicePayment
   * @param invoiceTermPayments
   * @return
   */
  public BigDecimal computeInvoicePaymentAmount(
      InvoicePayment invoicePayment, List<InvoiceTermPayment> invoiceTermPayments)
      throws AxelorException;

  /**
   * Method to update invoice Payment Amount based on its invoiceTermPayments
   *
   * @param invoicePayment
   * @return
   */
  public InvoicePayment updateInvoicePaymentAmount(InvoicePayment invoicePayment)
      throws AxelorException;

  public void manageInvoiceTermFinancialDiscount(
      InvoiceTermPayment invoiceTermPayment,
      InvoiceTerm invoiceTerm,
      boolean applyFinancialDiscount);
}
