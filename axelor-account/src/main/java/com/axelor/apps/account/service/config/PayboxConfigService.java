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
package com.axelor.apps.account.service.config;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.PayboxConfig;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;

public class PayboxConfigService extends AccountConfigService  {

	public PayboxConfig getPayboxConfig(AccountConfig accountConfig) throws AxelorException  {

		PayboxConfig payboxConfig = accountConfig.getPayboxConfig();

		if(payboxConfig == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYBOX_CONFIG_1),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return payboxConfig;

	}


	public PayboxConfig getPayboxConfig(Company company) throws AxelorException  {

		AccountConfig accountConfig = super.getAccountConfig(company);

		return this.getPayboxConfig(accountConfig);

	}



	/******************************** PAYBOX ********************************************/


	public String getPayboxSite(PayboxConfig payboxConfig) throws AxelorException  {

		if(payboxConfig.getPayboxSite() == null || payboxConfig.getPayboxSite().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYBOX_CONFIG_2),
					GeneralServiceImpl.EXCEPTION, payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}

		return payboxConfig.getPayboxSite();
	}

	public String getPayboxRang(PayboxConfig payboxConfig) throws AxelorException  {

		if(payboxConfig.getPayboxRang() == null || payboxConfig.getPayboxRang().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYBOX_CONFIG_3),
					GeneralServiceImpl.EXCEPTION, payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}

		return payboxConfig.getPayboxRang();
	}

	public String getPayboxDevise(PayboxConfig payboxConfig) throws AxelorException  {

		if(payboxConfig.getPayboxDevise() == null || payboxConfig.getPayboxDevise().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYBOX_CONFIG_4),
					GeneralServiceImpl.EXCEPTION, payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}

		return payboxConfig.getPayboxDevise();
	}

	public String getPayboxRetour(PayboxConfig payboxConfig) throws AxelorException  {

		if(payboxConfig.getPayboxRetour() == null || payboxConfig.getPayboxRetour().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYBOX_CONFIG_5),
					GeneralServiceImpl.EXCEPTION, payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}

		return payboxConfig.getPayboxRetour();
	}

	public String getPayboxRetourUrlEffectue(PayboxConfig payboxConfig) throws AxelorException  {

		if(payboxConfig.getPayboxRetourUrlEffectue() == null || payboxConfig.getPayboxRetourUrlEffectue().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYBOX_CONFIG_6),
					GeneralServiceImpl.EXCEPTION, payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}

		return payboxConfig.getPayboxRetourUrlEffectue();
	}

	public String getPayboxRetourUrlRefuse(PayboxConfig payboxConfig) throws AxelorException  {

		if(payboxConfig.getPayboxRetourUrlRefuse() == null || payboxConfig.getPayboxRetourUrlRefuse().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYBOX_CONFIG_7),
					GeneralServiceImpl.EXCEPTION, payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}

		return payboxConfig.getPayboxRetourUrlRefuse();
	}

	public String getPayboxRetourUrlAnnule(PayboxConfig payboxConfig) throws AxelorException  {

		if(payboxConfig.getPayboxRetourUrlAnnule() == null || payboxConfig.getPayboxRetourUrlAnnule().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYBOX_CONFIG_8),
					GeneralServiceImpl.EXCEPTION, payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}

		return payboxConfig.getPayboxRetourUrlAnnule();
	}

	public String getPayboxIdentifiant(PayboxConfig payboxConfig) throws AxelorException  {

		if(payboxConfig.getPayboxIdentifiant() == null || payboxConfig.getPayboxIdentifiant().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYBOX_CONFIG_9),
					GeneralServiceImpl.EXCEPTION, payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}

		return payboxConfig.getPayboxIdentifiant();
	}

	public String getPayboxHashSelect(PayboxConfig payboxConfig) throws AxelorException  {

		if(payboxConfig.getPayboxHashSelect() == null || payboxConfig.getPayboxHashSelect().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYBOX_CONFIG_10),
					GeneralServiceImpl.EXCEPTION, payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}

		return payboxConfig.getPayboxHashSelect();
	}

	public String getPayboxHmac(PayboxConfig payboxConfig) throws AxelorException  {

		if(payboxConfig.getPayboxHmac() == null || payboxConfig.getPayboxHmac().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYBOX_CONFIG_11),
					GeneralServiceImpl.EXCEPTION, payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}

		return payboxConfig.getPayboxHmac();
	}

	public String getPayboxUrl(PayboxConfig payboxConfig) throws AxelorException  {

		if(payboxConfig.getPayboxUrl() == null || payboxConfig.getPayboxUrl().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYBOX_CONFIG_12),
					GeneralServiceImpl.EXCEPTION, payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}

		return payboxConfig.getPayboxUrl();
	}

	public String getPayboxPublicKeyPath(PayboxConfig payboxConfig) throws AxelorException  {

		if(payboxConfig.getPayboxPublicKeyPath() == null || payboxConfig.getPayboxPublicKeyPath().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYBOX_CONFIG_13),
					GeneralServiceImpl.EXCEPTION, payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}

		return payboxConfig.getPayboxPublicKeyPath();
	}

	public String getPayboxDefaultEmail(PayboxConfig payboxConfig) throws AxelorException  {

		if(payboxConfig.getPayboxDefaultEmail() == null || payboxConfig.getPayboxDefaultEmail().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYBOX_CONFIG_14),
					GeneralServiceImpl.EXCEPTION, payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}

		return payboxConfig.getPayboxDefaultEmail();
	}


}
