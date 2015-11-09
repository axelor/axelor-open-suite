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
