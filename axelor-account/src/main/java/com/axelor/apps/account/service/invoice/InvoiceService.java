/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.meta.CallMethod;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

/** InvoiceService est une classe implémentant l'ensemble des services de facturations. */
public interface InvoiceService {

  /**
   * Fetches the journal to apply to an invoice, based on the operationType and A.T.I amount
   *
   * @param invoice Invoice to fetch the journal for.
   * @return The suitable journal or null (!) if invoice's company is empty.
   * @throws AxelorException If operationTypeSelect is empty
   */
  Journal getJournal(Invoice invoice) throws AxelorException;

  /**
   * Fonction permettant de calculer l'intégralité d'une facture :
   *
   * <ul>
   *   <li>Détermine les taxes;
   *   <li>Détermine la TVA;
   *   <li>Détermine les totaux.
   * </ul>
   *
   * (Transaction)
   *
   * @param invoice Une facture.
   * @throws AxelorException
   */
  public Invoice compute(final Invoice invoice) throws AxelorException;

  /**
   * Validate an invoice.
   *
   * @param invoice
   * @param compute
   * @throws AxelorException
   */
  public void validate(Invoice invoice) throws AxelorException;

  /**
   * Ventilation comptable d'une facture. (Transaction)
   *
   * @param invoice Une facture.
   * @throws AxelorException
   */
  public void ventilate(Invoice invoice) throws AxelorException;

  /**
   * Validate and ventilate an invoice.
   *
   * @param invoice
   * @throws AxelorException
   */
  void validateAndVentilate(Invoice invoice) throws AxelorException;

  /**
   * Annuler une facture. (Transaction)
   *
   * @param invoice Une facture.
   * @throws AxelorException
   */
  public void cancel(Invoice invoice) throws AxelorException;

  /**
   * Procédure permettant d'impacter la case à cocher "Passage à l'huissier" sur l'écriture de
   * facture. (Transaction)
   *
   * @param invoice Une facture
   */
  public void usherProcess(Invoice invoice);

  String checkNotImputedRefunds(Invoice invoice) throws AxelorException;

  /**
   * Créer un avoir.
   *
   * <p>Un avoir est une facture "inversée". Tout le montant sont opposés à la facture originale.
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  public Invoice createRefund(Invoice invoice) throws AxelorException;

  public void setDraftSequence(Invoice invoice) throws AxelorException;

  public Invoice mergeInvoiceProcess(
      List<Invoice> invoiceList,
      Company company,
      Currency currency,
      Partner partner,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition,
      TradingName tradingName,
      FiscalPosition fiscalPosition)
      throws AxelorException;

  public Invoice mergeInvoice(
      List<Invoice> invoiceList,
      Company company,
      Currency currency,
      Partner partner,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition,
      TradingName tradingName,
      FiscalPosition fiscalPosition)
      throws AxelorException;

  public Invoice mergeInvoiceProcess(
      List<Invoice> invoiceList,
      Company company,
      Currency currency,
      Partner partner,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition,
      TradingName tradingName,
      FiscalPosition fiscalPosition,
      String supplierInvoiceNb,
      LocalDate originDate)
      throws AxelorException;

  public Invoice mergeInvoice(
      List<Invoice> invoiceList,
      Company company,
      Currency currency,
      Partner partner,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition,
      TradingName tradingName,
      FiscalPosition fiscalPosition,
      String supplierInvoiceNb,
      LocalDate originDate)
      throws AxelorException;

  public List<InvoiceLine> getInvoiceLinesFromInvoiceList(List<Invoice> invoiceList);

  public void setInvoiceForInvoiceLines(List<InvoiceLine> invoiceLines, Invoice invoiceMerged);

  public void deleteOldInvoices(List<Invoice> invoiceList);

  public Invoice getInvoice(MoveLine moveLine);

  /**
   * Create the domain for the field {@link Invoice#advancePaymentInvoiceSet}
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  String createAdvancePaymentInvoiceSetDomain(Invoice invoice) throws AxelorException;

  /**
   * Return the set for the field {@link Invoice#advancePaymentInvoiceSet}
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  Set<Invoice> getDefaultAdvancePaymentInvoice(Invoice invoice) throws AxelorException;

  /**
   * Return the move lines from the advance payments on sale orders
   *
   * @param invoice
   * @return
   */
  List<MoveLine> getMoveLinesFromAdvancePayments(Invoice invoice) throws AxelorException;

  /**
   * Return the move lines from the advance payments from previous invoices
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  List<MoveLine> getMoveLinesFromInvoiceAdvancePayments(Invoice invoice) throws AxelorException;

  /**
   * Return the move line from the advance payment from related sale order lines.
   *
   * @param invoice
   * @return
   */
  List<MoveLine> getMoveLinesFromSOAdvancePayments(Invoice invoice);
  /**
   * Filter a set of advance payment invoice. If the amount of the payment is greater than the total
   * of the invoice, we filter it. If there is no remaining amount in the move lines of the advance
   * payment invoice, we filter it too.
   *
   * @param invoice
   * @param advancePaymentInvoices
   * @throws AxelorException
   */
  void filterAdvancePaymentInvoice(Invoice invoice, Set<Invoice> advancePaymentInvoices)
      throws AxelorException;

  /**
   * Get the bank details from the invoice's payment schedule, the invoice itself, or the partner's
   * default.
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  BankDetails getBankDetails(Invoice invoice) throws AxelorException;

  /**
   * @param invoice
   * @return {@link com.axelor.apps.base.db.repo.PriceListRepository#TYPE_SALE} OR {@link
   *     com.axelor.apps.base.db.repo.PriceListRepository#TYPE_PURCHASE}
   */
  int getPurchaseTypeOrSaleType(Invoice invoice);

  /**
   * Mass validate the given collection of invoice IDs.
   *
   * @param invoiceIds
   * @return pair of done/anomaly counts
   */
  Pair<Integer, Integer> massValidate(Collection<? extends Number> invoiceIds);

  /**
   * Mass validate and ventilate the given collection of invoice IDs.
   *
   * @param invoiceIds
   * @return pair of done/anomaly counts
   */
  Pair<Integer, Integer> massValidateAndVentilate(Collection<? extends Number> invoiceIds);

  /**
   * Mass ventilate the given collection of invoice IDs.
   *
   * @param invoiceIds
   * @return pair of done/anomaly counts
   */
  Pair<Integer, Integer> massVentilate(Collection<? extends Number> invoiceIds);

  public Boolean checkPartnerBankDetailsList(Invoice invoice);

  public String checkNotLetteredAdvancePaymentMoveLines(Invoice invoice) throws AxelorException;

  public void refusalToPay(
      Invoice invoice, CancelReason reasonOfRefusalToPay, String reasonOfRefusalToPayStr);

  @CallMethod
  LocalDate getFinancialDiscountDeadlineDate(Invoice invoice, FinancialDiscount financialDiscount);

  boolean checkInvoiceLinesAnalyticDistribution(Invoice invoice);

  boolean checkInvoiceLinesCutOffDates(Invoice invoice);

  boolean checkManageCutOffDates(Invoice invoice);

  void applyCutOffDates(Invoice invoice, LocalDate cutOffStartDate, LocalDate cutOffEndDate);

  boolean isSelectedPfpValidatorEqualsPartnerPfpValidator(Invoice invoice);

  public void validatePfp(Long invoiceId) throws AxelorException;

  void updateUnpaidInvoiceTerms(Invoice invoice);

  /**
   * Check invoice terms before opening payment wizard
   *
   * @param invoice
   * @return true if there would be at least one invoice term in the invoice payment
   */
  boolean checkInvoiceTerms(Invoice invoice) throws AxelorException;

  void updateInvoiceTermsParentFields(Invoice invoice);

  Invoice computeEstimatedPaymentDate(Invoice invoice);

  void updateSubrogationPartner(Invoice invoice);
}
