/**
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
package com.axelor.apps.bankpayment.service.config;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;

public class AccountConfigBankPaymentService extends AccountConfigService  {


	public Account getExternalBankToBankAccount(AccountConfig accountConfig) throws AxelorException  {
		if (accountConfig.getExternalBankToBankAccount() == null) {
			throw new AxelorException(accountConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.ACCOUNT_CONFIG_EXTERNAL_BANK_TO_BANK_ACCOUNT), AppBaseServiceImpl.EXCEPTION,accountConfig.getCompany().getName());
		}
		return accountConfig.getExternalBankToBankAccount();

	} 
	
	public Account getInternalBankToBankAccount(AccountConfig accountConfig) throws AxelorException  {
		if (accountConfig.getInternalBankToBankAccount() == null) {
			throw new AxelorException(accountConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.ACCOUNT_CONFIG_INTERNAL_BANK_TO_BANK_ACCOUNT), AppBaseServiceImpl.EXCEPTION,accountConfig.getCompany().getName());
		}
		return accountConfig.getInternalBankToBankAccount();

	} 


    /******** Bank Order Sequences ********/
	public Sequence getSepaCreditTransSequence(AccountConfig accountConfig) throws AxelorException {
		if (accountConfig.getSepaCreditTransSequence() == null) {
			throw new AxelorException(accountConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_5), AppBaseServiceImpl.EXCEPTION, accountConfig.getCompany().getName());
		}
		return accountConfig.getSepaCreditTransSequence();
	}

	public Sequence getSepaDirectDebitSequence(AccountConfig accountConfig) throws AxelorException {
		if (accountConfig.getSepaDirectDebitSequence() == null) {
			throw new AxelorException(accountConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_6), AppBaseServiceImpl.EXCEPTION, accountConfig.getCompany().getName());
		}
		return accountConfig.getSepaDirectDebitSequence();
	}

	public Sequence getIntCreditTransSequence(AccountConfig accountConfig) throws AxelorException {
		if (accountConfig.getIntCreditTransSequence() == null) {
			throw new AxelorException(accountConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_7), AppBaseServiceImpl.EXCEPTION, accountConfig.getCompany().getName());
		}
		return accountConfig.getIntCreditTransSequence();
	}

	public Sequence getIntDirectDebitSequence(AccountConfig accountConfig) throws AxelorException {
		if (accountConfig.getIntDirectDebitSequence() == null) {
			throw new AxelorException(accountConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_8), AppBaseServiceImpl.EXCEPTION, accountConfig.getCompany().getName());
		}
		return accountConfig.getIntDirectDebitSequence();
	}
	
	public Sequence getNatTreasuryTransSequence(AccountConfig accountConfig) throws AxelorException {
		if (accountConfig.getNatTreasuryTransSequence() == null) {
			throw new AxelorException(accountConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_10), AppBaseServiceImpl.EXCEPTION, accountConfig.getCompany().getName());
		}
		return accountConfig.getNatTreasuryTransSequence();
	}
	
	public Sequence getIntTreasuryTransSequence(AccountConfig accountConfig) throws AxelorException {
		if (accountConfig.getIntTreasuryTransSequence() == null) {
			throw new AxelorException(accountConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_9), AppBaseServiceImpl.EXCEPTION, accountConfig.getCompany().getName());
		}
		return accountConfig.getIntTreasuryTransSequence();
	}
	
	public Sequence getOtherBankOrderSequence(AccountConfig accountConfig) throws AxelorException {
		if (accountConfig.getOtherBankOrderSequence() == null) {
			throw new AxelorException(accountConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_11), AppBaseServiceImpl.EXCEPTION, accountConfig.getCompany().getName());
		}
		return accountConfig.getOtherBankOrderSequence();
	}
	
	
	/******************************** BANK ORDERS *********************************************/
	public User getDefaultSignatoryUser(AccountConfig accountConfig) throws AxelorException {
		if (accountConfig.getDefaultSignatoryUser() == null) {
			throw new AxelorException(accountConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.ACCOUNT_CONFIG_41), AppBaseServiceImpl.EXCEPTION,accountConfig.getCompany().getName());
		}
		return accountConfig.getDefaultSignatoryUser();
	}

    /**
     * Get ICS number.
     *
     * @param accountConfig
     * @return
     * @throws AxelorException
     */
    public String getIcsNumber(AccountConfig accountConfig) throws AxelorException {
        if (Strings.isNullOrEmpty(accountConfig.getIcsNumber())) {
            throw new AxelorException(accountConfig, IException.CONFIGURATION_ERROR,
                    I18n.get(IExceptionMessage.ACCOUNT_CONFIG_MISSING_ICS_NUMBER), AppBaseServiceImpl.EXCEPTION,
                    accountConfig.getCompany().getName());
        }
        return accountConfig.getIcsNumber();
    }

}
