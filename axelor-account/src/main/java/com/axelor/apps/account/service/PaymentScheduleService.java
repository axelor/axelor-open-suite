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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface PaymentScheduleService {
  PaymentSchedule createPaymentSchedule(
      Partner partner, Company company, Set<Invoice> invoices, LocalDate startDate, int nbrTerm)
      throws AxelorException;

  PaymentSchedule createPaymentSchedule(
      Partner partner,
      Invoice invoice,
      Company company,
      LocalDate date,
      LocalDate startDate,
      int nbrTerm,
      BankDetails bankDetails,
      PaymentMode paymentMode)
      throws AxelorException;

  String getPaymentScheduleSequence(Company company) throws AxelorException;

  BigDecimal getInvoiceTermTotal(PaymentSchedule paymentSchedule);

  void updatePaymentSchedule(PaymentSchedule paymentSchedule, BigDecimal inTaxTotal);

  PaymentSchedule createPaymentSchedule(
      Partner partner,
      Company company,
      LocalDate date,
      LocalDate firstTermDate,
      BigDecimal initialInTaxAmount,
      int nbrTerm,
      BankDetails bankDetails,
      PaymentMode paymentMode)
      throws AxelorException;

  List<MoveLine> getPaymentSchedulerMoveLineToPay(PaymentSchedule paymentSchedule);

  void validatePaymentSchedule(PaymentSchedule paymentSchedule) throws AxelorException;

  void updateInvoices(PaymentSchedule paymentSchedule);

  void updateInvoice(Invoice invoice, PaymentSchedule paymentSchedule);

  void cancelPaymentSchedule(PaymentSchedule paymentSchedule);

  boolean isLastSchedule(PaymentScheduleLine paymentScheduleLine);

  void closePaymentSchedule(PaymentSchedule paymentSchedule) throws AxelorException;

  void closePaymentScheduleIfAllPaid(PaymentSchedule paymentSchedule) throws AxelorException;

  LocalDate getMostOldDatePaymentScheduleLine(List<PaymentScheduleLine> paymentScheduleLineList);

  LocalDate getMostRecentDatePaymentScheduleLine(List<PaymentScheduleLine> paymentScheduleLineList);

  void createPaymentScheduleLines(PaymentSchedule paymentSchedule);

  void initCollection(PaymentSchedule paymentSchedule);

  void toCancelPaymentSchedule(PaymentSchedule paymentSchedule);

  /**
   * Get partner's bank details.
   *
   * @param paymentSchedule
   * @return
   * @throws AxelorException
   */
  BankDetails getBankDetails(PaymentSchedule paymentSchedule) throws AxelorException;

  /**
   * Check total line amount.
   *
   * @param paymentSchedule
   * @throws AxelorException
   */
  void checkTotalLineAmount(PaymentSchedule paymentSchedule) throws AxelorException;
}
