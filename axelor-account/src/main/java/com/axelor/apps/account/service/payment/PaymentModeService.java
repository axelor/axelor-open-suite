/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.payment;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface PaymentModeService {

  public Account getPaymentModeAccount(
      PaymentMode paymentMode, Company company, BankDetails bankDetails) throws AxelorException;

  public AccountManagement getAccountManagement(
      PaymentMode paymentMode, Company company, BankDetails bankDetails);

  public Sequence getPaymentModeSequence(
      PaymentMode paymentMode, Company company, BankDetails bankDetails) throws AxelorException;

  public Journal getPaymentModeJournal(
      PaymentMode paymentMode, Company company, BankDetails bankDetails) throws AxelorException;

  /**
   * @param paymentMode
   * @param company
   * @return list of bankdetails in the payment mode for the given company.
   */
  public List<BankDetails> getCompatibleBankDetailsList(PaymentMode paymentMode, Company company);

  /**
   * Returns a payment mode with the same type as the given payment mode, but with reversed in or
   * out status. Return null if no payment mode were found or if the given payment mode is null.
   */
  public PaymentMode reverseInOut(PaymentMode paymentMode);
}
