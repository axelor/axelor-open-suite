/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceTermService {

  public Invoice computeInvoiceTerms(Invoice invoice) throws AxelorException;

  /**
   * Method that computes invoiceTerm fields based on the payment condition line rules
   *
   * @param invoice
   * @param paymentConditionLine
   * @return
   * @throws AxelorException
   */
  public InvoiceTerm computeInvoiceTerm(Invoice invoice, PaymentConditionLine paymentConditionLine)
      throws AxelorException;

  /**
   * Method to init invoice terms due dates
   *
   * @param invoice
   * @param invoiceDate
   * @return
   */
  public Invoice setDueDates(Invoice invoice, LocalDate invoiceDate);

  /**
   * Method that returns unpaid invoiceTerms (isPaid != true) of an invoice
   *
   * @param invoice
   * @return
   */
  public List<InvoiceTerm> getUnpaidInvoiceTerms(Invoice invoice);

  /**
   * Method that filters invoiceTerm List and returns only invoice terms with holdback status same
   * as first invoice term of the list.
   *
   * @param invoiceTerms
   * @return
   */
  public List<InvoiceTerm> filterInvoiceTermsByHoldBack(List<InvoiceTerm> invoiceTerms);

  /**
   * Method that returns only unpaid invoice terms of an invoice having holdback status same as
   * first returned invoice term
   *
   * @param invoice
   * @return
   */
  public List<InvoiceTerm> getUnpaidInvoiceTermsFiltered(Invoice invoice);

  /**
   * Update amount remaining and paid status
   *
   * @param invoicePayment
   */
  public void updateInvoiceTermsPaidAmount(InvoicePayment invoicePayment) throws AxelorException;

  /**
   * Update amount remaining and paid status after unreconcile
   *
   * @param invoicePayment
   */
  @Transactional(rollbackOn = {Exception.class})
  public void updateInvoiceTermsAmountRemaining(InvoicePayment invoicePayment)
      throws AxelorException;

  /**
   * Check if invoice term were customized
   *
   * @param invoice
   * @return
   */
  public boolean checkIfCustomizedInvoiceTerms(Invoice invoice);

  /**
   * Check if the sum of invoice terms amounts equals invoice inTaxTotal
   *
   * @param invoice
   * @throws AxelorException
   */
  public boolean checkInvoiceTermsSum(Invoice invoice) throws AxelorException;

  /**
   * Check if the sum of invoice terms percentage equals 100
   *
   * @param invoice
   * @throws AxelorException
   */
  public boolean checkInvoiceTermsPercentageSum(Invoice invoice) throws AxelorException;
}
