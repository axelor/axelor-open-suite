/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.CfonbConfig;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class CfonbConfigService extends AccountConfigService  {
	
	private static final Logger LOG = LoggerFactory.getLogger(CfonbConfigService.class);

	
	public CfonbConfig getCfonbConfig(AccountConfig accountConfig) throws AxelorException  {
		
		CfonbConfig cfonbConfig = accountConfig.getCfonbConfig();
		
		if(cfonbConfig == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return cfonbConfig;
		
	}
	
	
	public CfonbConfig getCfonbConfig(Company company) throws AxelorException  {
		
		AccountConfig accountConfig = super.getAccountConfig(company);
		
		return this.getCfonbConfig(accountConfig);
		
	}
	
	
	
	/******************************** EXPORT CFONB ********************************************/
	
	
	public String getSenderRecordCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		String senderRecordCodeExportCFONB = cfonbConfig.getSenderRecordCodeExportCFONB();
		
		if(senderRecordCodeExportCFONB == null || senderRecordCodeExportCFONB.isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code enregistrement émetteur CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return senderRecordCodeExportCFONB;
		
	}
	
	public void getSenderNumExportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getSenderNumExportCFONB() == null || cfonbConfig.getSenderNumExportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Numéro d'émetteur CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getSenderNameCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getSenderNameCodeExportCFONB() == null || cfonbConfig.getSenderNameCodeExportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Nom/Raison sociale émetteur CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getRecipientRecordCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getRecipientRecordCodeExportCFONB() == null || cfonbConfig.getRecipientRecordCodeExportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code enregistrement destinataire CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getTotalRecordCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getTotalRecordCodeExportCFONB() == null || cfonbConfig.getTotalRecordCodeExportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code enregistrement total CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getTransferOperationCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getTransferOperationCodeExportCFONB() == null || cfonbConfig.getTransferOperationCodeExportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code opération Virement CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getDirectDebitOperationCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getDirectDebitOperationCodeExportCFONB() == null || cfonbConfig.getDirectDebitOperationCodeExportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code opération Prélèvement CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	/******************************** IMPORT CFONB ********************************************/
	
	public void getHeaderRecordCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getHeaderRecordCodeImportCFONB() == null || cfonbConfig.getHeaderRecordCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code enregistrement en-tête CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getDetailRecordCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getDetailRecordCodeImportCFONB() == null || cfonbConfig.getDetailRecordCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code enregistrement detail CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getEndingRecordCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getEndingRecordCodeImportCFONB() == null || cfonbConfig.getEndingRecordCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code enregistrement fin CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getTransferOperationCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getTransferOperationCodeImportCFONB() == null || cfonbConfig.getTransferOperationCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code opération Virement rejeté CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getDirectDebitOperationCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getDirectDebitOperationCodeImportCFONB() == null || cfonbConfig.getDirectDebitOperationCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code opération Prélèvement impayé CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getIpoRejectOperationCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getIpoRejectOperationCodeImportCFONB() == null || cfonbConfig.getIpoRejectOperationCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code opération TIP impayé CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getIpoAndChequeOperationCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getIpoAndChequeOperationCodeImportCFONB() == null || cfonbConfig.getIpoAndChequeOperationCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code opération TIP + chèque CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getIpoOperationCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getIpoOperationCodeImportCFONB() == null || cfonbConfig.getIpoOperationCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code opération TIP CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	
	
}
