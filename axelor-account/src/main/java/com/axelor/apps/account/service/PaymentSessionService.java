/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface PaymentSessionService {

  public String computeName(PaymentSession paymentSession);

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
