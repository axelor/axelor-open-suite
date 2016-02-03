/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.config;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;

public class AccountConfigHRService extends AccountConfigService{
	
	public Journal getExpenseJournal(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getExpenseJournal() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.EXPENSE_JOURNAL),  
					accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getExpenseJournal();
	}
	
	public Account getExpenseEmployeeAccount(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getExpenseEmployeeAccount() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.EXPENSE_ACCOUNT),  
					accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getExpenseEmployeeAccount();
	}
	
	public Account getExpenseTaxAccount(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getExpenseTaxAccount() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.EXPENSE_ACCOUNT_TAX),  
					accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getExpenseTaxAccount();
	}

}
