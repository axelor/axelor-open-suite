/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.PayboxConfig;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class PayboxConfigService extends AccountConfigService  {
	
	private static final Logger LOG = LoggerFactory.getLogger(PayboxConfigService.class);

	
	public PayboxConfig getPayboxConfig(AccountConfig accountConfig) throws AxelorException  {
		
		PayboxConfig payboxConfig = accountConfig.getPayboxConfig();
		
		if(payboxConfig == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer Paybox pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
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
					GeneralService.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxSite();
	}
	
	public String getPayboxRang(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxRang() == null || payboxConfig.getPayboxRang().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Numéro de rang pour la configuration Paybox %s",
					GeneralService.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxRang();
	}
	
	public String getPayboxDevise(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxDevise() == null || payboxConfig.getPayboxDevise().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Devise des transactions pour la configuration Paybox %s",
					GeneralService.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxDevise();
	}
	
	public String getPayboxRetour(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxRetour() == null || payboxConfig.getPayboxRetour().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Liste des variables à retourner par Paybox pour la configuration Paybox %s",
					GeneralService.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxRetour();
	}
	
	public String getPayboxRetourUrlEffectue(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxRetourUrlEffectue() == null || payboxConfig.getPayboxRetourUrlEffectue().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Url retourner par Paybox une fois le paiement effectué pour la configuration Paybox %s",
					GeneralService.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxRetourUrlEffectue();
	}
	
	public String getPayboxRetourUrlRefuse(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxRetourUrlRefuse() == null || payboxConfig.getPayboxRetourUrlRefuse().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Url retourner par Paybox une fois le paiement refusé pour la configuration Paybox %s",
					GeneralService.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxRetourUrlRefuse();
	}
	
	public String getPayboxRetourUrlAnnule(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxRetourUrlAnnule() == null || payboxConfig.getPayboxRetourUrlAnnule().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Url retourner par Paybox une fois le paiement annulé pour la configuration Paybox %s",
					GeneralService.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxRetourUrlAnnule();
	}
	
	public String getPayboxIdentifiant(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxIdentifiant() == null || payboxConfig.getPayboxIdentifiant().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Identifiant interne pour la configuration Paybox %s",
					GeneralService.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxIdentifiant();
	}
	
	public String getPayboxHashSelect(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxHashSelect() == null || payboxConfig.getPayboxHashSelect().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez selectionner un Type d'algorithme de hachage utilisé lors du calcul de l'empreinte pour la configuration Paybox %s",
					GeneralService.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxHashSelect();
	}
	
	public String getPayboxHmac(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxHmac() == null || payboxConfig.getPayboxHmac().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Signature calculée avec la clé secrète pour la configuration Paybox %s",
					GeneralService.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxHmac();
	}
	
	public String getPayboxUrl(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxUrl() == null || payboxConfig.getPayboxUrl().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Url de l'environnement pour la configuration Paybox %s",
					GeneralService.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxUrl();
	}
	
	public String getPayboxPublicKeyPath(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxPublicKeyPath() == null || payboxConfig.getPayboxPublicKeyPath().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Chemin de la clé publique Paybox pour la configuration Paybox %s",
					GeneralService.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxPublicKeyPath();
	}
	
	public String getPayboxDefaultEmail(PayboxConfig payboxConfig) throws AxelorException  {
		
		if(payboxConfig.getPayboxDefaultEmail() == null || payboxConfig.getPayboxDefaultEmail().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Email de back-office Axelor pour Paybox pour la configuration Paybox %s",
					GeneralService.getExceptionAccountingMsg(), payboxConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return payboxConfig.getPayboxDefaultEmail();
	}
	
	
}
