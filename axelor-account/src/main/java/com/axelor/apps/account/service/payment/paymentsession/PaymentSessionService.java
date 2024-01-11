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
package com.axelor.apps.account.service.payment.paymentsession;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import java.util.List;

public interface PaymentSessionService {

  public String computeName(PaymentSession paymentSession) throws AxelorException;

  public void setBankDetails(PaymentSession paymentSession);

  public void setJournal(PaymentSession paymentSession);

  public boolean hasUnselectedInvoiceTerm(PaymentSession paymentSession);

  List<BankDetails> getBankDetails(PaymentSession paymentSession);

  List<Journal> getJournals(PaymentSession paymentSession);

  public int removeMultiplePaymentSessions(List<Long> paymentSessionIds) throws AxelorException;

  public void selectAll(PaymentSession paymentSession) throws AxelorException;

  public void unSelectAll(PaymentSession paymentSession) throws AxelorException;

  public boolean hasInvoiceTerm(PaymentSession paymentSession);

  public void searchEligibleTerms(PaymentSession paymentSession) throws AxelorException;
}
