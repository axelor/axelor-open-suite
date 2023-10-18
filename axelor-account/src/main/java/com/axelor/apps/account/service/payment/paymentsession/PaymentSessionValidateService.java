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
package com.axelor.apps.account.service.payment.paymentsession;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public interface PaymentSessionValidateService {
  public int checkValidTerms(PaymentSession paymentSession);

  public int processPaymentSession(
      PaymentSession paymentSession,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList)
      throws AxelorException;

  public StringBuilder generateFlashMessage(PaymentSession paymentSession, int moveCount);

  public List<Partner> getPartnersWithNegativeAmount(PaymentSession paymentSession)
      throws AxelorException;

  public void reconciledInvoiceTermMoves(
      PaymentSession paymentSession,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList)
      throws AxelorException;

  public boolean checkIsHoldBackWithRefund(PaymentSession paymentSession) throws AxelorException;

  public StringBuilder processInvoiceTerms(PaymentSession paymentSession) throws AxelorException;

  boolean isEmpty(PaymentSession paymentSession);

  public List<InvoiceTerm> getInvoiceTermsWithInActiveBankDetails(PaymentSession paymentSession);

  void createAndReconcileMoveLineFromPair(
      PaymentSession paymentSession,
      Move move,
      InvoiceTerm invoiceTerm,
      Pair<InvoiceTerm, BigDecimal> pair,
      AccountConfig accountConfig,
      boolean out,
      Map<Move, BigDecimal> paymentAmountMap)
      throws AxelorException;

  public boolean containsCompensativeInvoiceTerm(
      List<InvoiceTerm> invoiceTermList, PaymentSession paymentSession);

  public LocalDate getAccountingDate(PaymentSession paymentSession, InvoiceTerm invoiceTerm);
}
