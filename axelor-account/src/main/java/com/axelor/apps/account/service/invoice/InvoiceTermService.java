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
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
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
   * Method that creates a customized invoiceTerm
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  public InvoiceTerm initCustomizedInvoiceTerm(Invoice invoice, InvoiceTerm invoiceTerm);

  /**
   * Method to initialize invoice terms due dates
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
   * Return the latest invoice terms due date by ignoring holdback invoice terms Return invoice due
   * date if no invoice terms found
   *
   * @param invoice
   * @return
   */
  public LocalDate getLatestInvoiceTermDueDate(Invoice invoice);

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

  /**
   * compute the sum of invoice terms percentages
   *
   * @param invoice
   * @throws AxelorException
   */
  public BigDecimal computePercentageSum(Invoice invoice);

  /**
   * Update invoice terms financial discount if not paid with invoice financial discount
   *
   * @param invoice
   * @return
   */
  public List<InvoiceTerm> updateFinancialDiscount(Invoice invoice);

  /**
   * Initialize invoiceTerms sequences based on due date the method sorts the invoice term list
   * based on due date
   *
   * @param invoice
   */
  public void initInvoiceTermsSequence(Invoice invoice);

  /**
   * check if invoice term creation is prohibited returns true if prohibited
   *
   * @param invoice
   * @return
   */
  public boolean checkInvoiceTermCreationConditions(Invoice invoice);

  /**
   * check if invoice term deletion is prohibited returns true if prohibited
   *
   * @param invoice
   * @return
   */
  public boolean checkInvoiceTermDeletionConditions(Invoice invoice);

  /**
   * checks if there is deleted hold back invoice terms while invoice ventilated
   *
   * @param invoice
   * @return
   */
  public boolean checkIfThereIsDeletedHoldbackInvoiceTerms(Invoice invoice);

  /**
   * return existing moveLine related to invoiceTerm with isHoldBack = false
   *
   * @param invoice
   * @return
   */
  public MoveLine getExistingInvoiceTermMoveLine(Invoice invoice);

  public void refusalToPay(
      InvoiceTerm invoiceTerm, CancelReason reasonOfRefusalToPay, String reasonOfRefusalToPayStr);

  public BigDecimal computeCustomizedPercentage(BigDecimal amount, BigDecimal inTaxTotal);

  public void generateInvoiceTerm(
      InvoiceTerm originalInvoiceTerm,
      BigDecimal invoiceAmount,
      BigDecimal pfpGrantedAmount,
      PfpPartialReason partialReason);
}
