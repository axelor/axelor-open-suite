/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import java.util.List;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;


public interface AccountingSituationService	{

	boolean checkAccountingSituationList(List<AccountingSituation> accountingSituationList, Company company);
	List<AccountingSituation> createAccountingSituation(Partner partner) throws AxelorException;
	AccountingSituation getAccountingSituation(Partner partner, Company company);
	AccountingSituation createAccountingSituation(Partner partner, Company company) throws AxelorException;
	String createDomainForBankDetails(AccountingSituation accountingSituation, boolean isInBankDetails);
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
	 * @param company
	 * @param partner
	 * @return
	 */
	BankDetails getCompanySalesBankDetails(Company company, Partner partner);

}
