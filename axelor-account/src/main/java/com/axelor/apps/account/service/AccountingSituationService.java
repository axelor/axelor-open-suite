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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.meta.CallMethod;

public interface AccountingSituationService {

  AccountingSituation getAccountingSituation(Partner partner, Company company);

  String createDomainForBankDetails(
      AccountingSituation accountingSituation, boolean isInBankDetails);

  void updateCustomerCredit(Partner partner) throws AxelorException;

  /**
   * Get customer account from accounting situation or account config.
   *
   * @param partner
   * @param company
   * @return
   */
  Account getCustomerAccount(Partner partner, Company company) throws AxelorException;

  /**
   * Get supplier account from accounting situation or account config.
   *
   * @param partner
   * @param company
   * @return
   */
  Account getSupplierAccount(Partner partner, Company company) throws AxelorException;

  /**
   * Get employee account from accounting situation or account config.
   *
   * @param partner
   * @param company
   * @return
   */
  Account getEmployeeAccount(Partner partner, Company company) throws AxelorException;

  /**
   * Return bank details for sales to <code>partner</code> (took from SaleOrder.xml).
   *
   * @param company
   * @param partner
   * @return
   */
  @CallMethod
  BankDetails getCompanySalesBankDetails(Company company, Partner partner);

  /**
   * Get holdback customer account from accounting situation or account config.
   *
   * @param partner
   * @param company
   * @return
   */
  Account getHoldBackCustomerAccount(Partner partner, Company company) throws AxelorException;

  /**
   * Get holdback supplier account from accounting situation or account config.
   *
   * @param partner
   * @param company
   * @return
   */
  Account getHoldBackSupplierAccount(Partner partner, Company company) throws AxelorException;

  void setHoldBackAccounts(AccountingSituation accountingSituation, Partner partner)
      throws AxelorException;

  int determineVatSystemSelect(AccountingSituation accountingSituation, Account account)
      throws AxelorException;

  int determineVatSystemSelect(
      AccountingSituation accountingSituation, InvoiceLineTax invoiceLineTax)
      throws AxelorException;

  /**
   * Fetches suitable account for partner bound to the invoice, depending in the partner and the
   * type of invoice, and if holdback.
   *
   * @param invoice Invoice to fetch the partner account for
   * @return null if the invoice does not contains enough information to determine the partner
   *     account.
   * @throws AxelorException
   */
  Account getPartnerAccount(Invoice invoice, boolean isHoldback) throws AxelorException;
}
