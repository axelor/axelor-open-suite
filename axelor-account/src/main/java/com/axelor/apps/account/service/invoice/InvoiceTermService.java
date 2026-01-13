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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.auth.db.User;
import com.axelor.dms.db.DMSFile;
import com.axelor.meta.CallMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

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

  @CallMethod
  boolean getPfpValidatorUserCondition(Invoice invoice, MoveLine moveLine) throws AxelorException;

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
      InvoiceTermPayment invoiceTermPayment,
      Map<InvoiceTerm, Integer> invoiceTermPfpValidateStatusSelectMap)
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
   * @param invoiceTermList
   * @return
   */
  public boolean checkIfCustomizedInvoiceTerms(List<InvoiceTerm> invoiceTermList);

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

  MoveLine recomputeFreeDueDates(MoveLine moveLine, LocalDate dueDate);

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

  void setCustomizedAmounts(InvoiceTerm invoiceTerm);

  public List<InvoiceTerm> reconcileMoveLineInvoiceTermsWithFullRollBack(
      List<InvoiceTerm> invoiceTermList,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefund)
      throws AxelorException;

  InvoiceTerm updateInvoiceTermsAmountsSessionPart(InvoiceTerm invoiceTerm, boolean isRefund);

  void roundPercentages(List<InvoiceTerm> invoiceTermList, BigDecimal total);

  public String getPfpValidatorUserDomain(Partner partner, Company company);

  public BigDecimal getTotalInvoiceTermsAmount(MoveLine moveLine);

  BigDecimal getTotalInvoiceTermsAmount(
      MoveLine moveLine, Account holdbackAccount, boolean holdback);

  void updateFromMoveHeader(Move move, InvoiceTerm invoiceTerm);

  LocalDate getDueDate(List<InvoiceTerm> invoiceTermList, LocalDate defaultDate);

  void toggle(List<InvoiceTerm> invoiceTermList, boolean value) throws AxelorException;

  BigDecimal roundUpLastInvoiceTerm(
      List<InvoiceTerm> invoiceTermList, BigDecimal total, boolean isCompanyAmount)
      throws AxelorException;

  @CallMethod
  boolean isMultiCurrency(InvoiceTerm invoiceTerm);

  List<InvoiceTermPayment> updateInvoiceTerms(
      List<InvoiceTerm> invoiceTermList,
      InvoicePayment invoicePayment,
      BigDecimal amount,
      Reconcile reconcile,
      Map<InvoiceTerm, Integer> invoiceTermPfpValidateStatusSelectMap)
      throws AxelorException;

  List<InvoiceTerm> recomputeInvoiceTermsPercentage(
      List<InvoiceTerm> invoiceTermList, BigDecimal total);

  InvoiceTerm initInvoiceTermWithParents(InvoiceTerm invoiceTerm) throws AxelorException;

  boolean setShowFinancialDiscount(InvoiceTerm invoiceTerm);

  boolean isPaymentConditionFree(InvoiceTerm invoiceTerm);

  void payInvoiceTerms(List<InvoiceTerm> invoiceTermList);

  List<DMSFile> getLinkedDmsFile(InvoiceTerm invoiceTerm);

  void computeCustomizedPercentage(InvoiceTerm invoiceTerm);

  void computeInvoiceTermsDueDates(Invoice invoice) throws AxelorException;

  void computeInvoiceTermsDueDates(MoveLine moveLine, Move move);

  void checkAndComputeInvoiceTerms(Invoice invoice) throws AxelorException;

  List<InvoiceTerm> getInvoiceTermsFromMoveLine(List<InvoiceTerm> invoiceTermList);

  void updateInvoiceTermsAmountRemainingWithoutPayment(Reconcile reconcile, MoveLine moveLine)
      throws AxelorException;
}
