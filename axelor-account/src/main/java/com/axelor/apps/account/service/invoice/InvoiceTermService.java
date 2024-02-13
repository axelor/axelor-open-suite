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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.auth.db.User;
import com.axelor.meta.CallMethod;
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

  void computeCompanyAmounts(InvoiceTerm invoiceTerm, boolean isUpdate, boolean isHoldback);

  void computeFinancialDiscount(InvoiceTerm invoiceTerm, Invoice invoice);

  void computeFinancialDiscount(
      InvoiceTerm invoiceTerm,
      BigDecimal totalAmount,
      FinancialDiscount financialDiscount,
      BigDecimal financialDiscountAmount,
      BigDecimal remainingAmountAfterFinDiscount);

  /**
   * Method that creates a customized invoiceTerm
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  public InvoiceTerm initCustomizedInvoiceTerm(Invoice invoice, InvoiceTerm invoiceTerm)
      throws AxelorException;

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
  public List<InvoiceTerm> getUnpaidInvoiceTerms(Invoice invoice) throws AxelorException;

  List<InvoiceTerm> getUnpaidInvoiceTermsWithoutPfpCheck(Invoice invoice) throws AxelorException;

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
  public List<InvoiceTerm> getUnpaidInvoiceTermsFiltered(Invoice invoice) throws AxelorException;

  List<InvoiceTerm> getUnpaidInvoiceTermsFilteredWithoutPfpCheck(Invoice invoice)
      throws AxelorException;

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

  public void updateInvoiceTermsPaidAmount(
      InvoicePayment invoicePayment,
      InvoiceTerm invoiceTermToPay,
      InvoiceTermPayment invoiceTermPayment)
      throws AxelorException;

  /**
   * Update amount remaining and paid status after unreconcile
   *
   * @param invoicePayment
   */
  public void updateInvoiceTermsAmountRemaining(InvoicePayment invoicePayment)
      throws AxelorException;

  public void updateInvoiceTermsAmountRemaining(List<InvoiceTermPayment> invoiceTermPaymentList)
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

  InvoiceTerm initCustomizedInvoiceTerm(MoveLine moveLine, InvoiceTerm invoiceTerm, Move move)
      throws AxelorException;

  LocalDate computeDueDate(Move move, PaymentConditionLine paymentConditionLine);

  /**
   * return existing moveLine related to invoiceTerm with isHoldBack = false
   *
   * @param invoice
   * @return
   */
  public MoveLine getExistingInvoiceTermMoveLine(Invoice invoice);

  InvoiceTerm createInvoiceTerm(
      MoveLine moveLine,
      BankDetails bankDetails,
      User pfpUser,
      PaymentMode paymentMode,
      LocalDate date,
      BigDecimal amount,
      int sequence)
      throws AxelorException;

  InvoiceTerm createInvoiceTerm(
      Invoice invoice,
      Move move,
      MoveLine moveLine,
      BankDetails bankDetails,
      User pfpUser,
      PaymentMode paymentMode,
      LocalDate date,
      LocalDate estimatedPaymentDate,
      BigDecimal amount,
      BigDecimal percentage,
      int sequence,
      boolean isHoldBack)
      throws AxelorException;

  void setPfpStatus(InvoiceTerm invoiceTerm, Move move) throws AxelorException;

  void setParentFields(InvoiceTerm invoiceTerm, Move move, MoveLine moveLine, Invoice invoice);

  public void toggle(InvoiceTerm invoiceTerm, boolean value) throws AxelorException;

  public void computeAmountPaid(InvoiceTerm invoiceTerm);

  public BigDecimal computeCustomizedPercentage(BigDecimal amount, BigDecimal inTaxTotal);

  BigDecimal computeCustomizedPercentageUnscaled(BigDecimal amount, BigDecimal inTaxTotal);

  public BigDecimal getFinancialDiscountTaxAmount(InvoiceTerm invoiceTerm) throws AxelorException;

  BigDecimal getAmountRemaining(InvoiceTerm invoiceTerm, LocalDate date, boolean isCompanyCurrency);

  boolean setCustomizedAmounts(
      InvoiceTerm invoiceTerm, List<InvoiceTerm> invoiceTermList, BigDecimal total);

  public List<InvoiceTerm> reconcileMoveLineInvoiceTermsWithFullRollBack(
      List<InvoiceTerm> invoiceTermList) throws AxelorException;

  void reconcileAndUpdateInvoiceTermsAmounts(
      InvoiceTerm invoiceTermFromInvoice, InvoiceTerm invoiceTermFromRefund) throws AxelorException;

  List<InvoiceTerm> filterNotAwaitingPayment(List<InvoiceTerm> invoiceTermList);

  boolean isNotAwaitingPayment(InvoiceTerm invoiceTerm);

  boolean isEnoughAmountToPay(List<InvoiceTerm> invoiceTermList, BigDecimal amount, LocalDate date);

  void roundPercentages(List<InvoiceTerm> invoiceTermList, BigDecimal total);

  public User getPfpValidatorUser(Partner partner, Company company);

  public String getPfpValidatorUserDomain(Partner partner, Company company);

  public BigDecimal getTotalInvoiceTermsAmount(MoveLine moveLine);

  BigDecimal getTotalInvoiceTermsAmount(
      MoveLine moveLine, Account holdbackAccount, boolean holdback);

  void updateFromMoveHeader(Move move, InvoiceTerm invoiceTerm);

  boolean isNotReadonly(InvoiceTerm invoiceTerm);

  boolean isNotReadonlyExceptPfp(InvoiceTerm invoiceTerm);

  LocalDate getDueDate(List<InvoiceTerm> invoiceTermList, LocalDate defaultDate);

  void toggle(List<InvoiceTerm> invoiceTermList, boolean value) throws AxelorException;

  BigDecimal roundUpLastInvoiceTerm(
      List<InvoiceTerm> invoiceTermList, BigDecimal total, boolean isCompanyAmount)
      throws AxelorException;

  @CallMethod
  boolean isMultiCurrency(InvoiceTerm invoiceTerm);

  List<InvoiceTerm> recomputeInvoiceTermsPercentage(
      List<InvoiceTerm> invoiceTermList, BigDecimal total);

  boolean getPfpValidatorUserCondition(Invoice invoice);

  BigDecimal adjustAmountInCompanyCurrency(
      List<InvoiceTerm> invoiceTermList,
      BigDecimal companyAmountRemaining,
      BigDecimal amountToPayInCompanyCurrency,
      BigDecimal amountToPay,
      BigDecimal currencyRate);
}
