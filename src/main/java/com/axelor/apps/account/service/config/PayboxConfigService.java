/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class PayboxConfigService extends AccountConfigService  {
	
	public PayboxConfig getPayboxConfig(AccountConfig accountConfig) throws AxelorException  {
		
		PayboxConfig payboxConfig = accountConfig.getPayboxConfig();
		
		if(payboxConfig == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer Paybox pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
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
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Numéro de site pour la configuration Paybox %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxSite();
	}
	
	public String getPayboxRang(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxRang() == null || payboxConfig.getPayboxRang().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Numéro de rang pour la configuration Paybox %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxRang();
	}
	
	public String getPayboxDevise(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxDevise() == null || payboxConfig.getPayboxDevise().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Devise des transactions pour la configuration Paybox %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxDevise();
	}
	
	public String getPayboxRetour(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxRetour() == null || payboxConfig.getPayboxRetour().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Liste des variables à retourner par Paybox pour la configuration Paybox %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxRetour();
	}
	
	public String getPayboxRetourUrlEffectue(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxRetourUrlEffectue() == null || payboxConfig.getPayboxRetourUrlEffectue().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Url retourner par Paybox une fois le paiement effectué pour la configuration Paybox %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxRetourUrlEffectue();
	}
	
	public String getPayboxRetourUrlRefuse(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxRetourUrlRefuse() == null || payboxConfig.getPayboxRetourUrlRefuse().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Url retourner par Paybox une fois le paiement refusé pour la configuration Paybox %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxRetourUrlRefuse();
	}
	
	public String getPayboxRetourUrlAnnule(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxRetourUrlAnnule() == null || payboxConfig.getPayboxRetourUrlAnnule().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Url retourner par Paybox une fois le paiement annulé pour la configuration Paybox %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxRetourUrlAnnule();
	}
	
	public String getPayboxIdentifiant(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxIdentifiant() == null || payboxConfig.getPayboxIdentifiant().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Identifiant interne pour la configuration Paybox %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxIdentifiant();
	}
	
	public String getPayboxHashSelect(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxHashSelect() == null || payboxConfig.getPayboxHashSelect().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez selectionner un Type d'algorithme de hachage utilisé lors du calcul de l'empreinte pour la configuration Paybox %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxHashSelect();
	}
	
	public String getPayboxHmac(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxHmac() == null || payboxConfig.getPayboxHmac().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Signature calculée avec la clé secrète pour la configuration Paybox %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxHmac();
	}
	
	public String getPayboxUrl(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxUrl() == null || payboxConfig.getPayboxUrl().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Url de l'environnement pour la configuration Paybox %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxUrl();
	}
	
	public String getPayboxPublicKeyPath(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxPublicKeyPath() == null || payboxConfig.getPayboxPublicKeyPath().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Chemin de la clé publique Paybox pour la configuration Paybox %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxPublicKeyPath();
	}
	
	public String getPayboxDefaultEmail(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxDefaultEmail() == null || payboxConfig.getPayboxDefaultEmail().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Email de back-office Axelor pour Paybox pour la configuration Paybox %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxDefaultEmail();
	}
	
	
}
