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
import com.axelor.apps.account.db.CfonbConfig;
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class CfonbConfigService extends AccountConfigService  {
	
	public CfonbConfig getCfonbConfig(AccountConfig accountConfig) throws AxelorException  {
		
		CfonbConfig cfonbConfig = accountConfig.getCfonbConfig();
		
		if(cfonbConfig == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
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
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return senderRecordCodeExportCFONB;
		
	}
	
	public void getSenderNumExportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getSenderNumExportCFONB() == null || cfonbConfig.getSenderNumExportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Numéro d'émetteur CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getSenderNameCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getSenderNameCodeExportCFONB() == null || cfonbConfig.getSenderNameCodeExportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Nom/Raison sociale émetteur CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getRecipientRecordCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getRecipientRecordCodeExportCFONB() == null || cfonbConfig.getRecipientRecordCodeExportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code enregistrement destinataire CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getTotalRecordCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getTotalRecordCodeExportCFONB() == null || cfonbConfig.getTotalRecordCodeExportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code enregistrement total CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getTransferOperationCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getTransferOperationCodeExportCFONB() == null || cfonbConfig.getTransferOperationCodeExportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code opération Virement CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getDirectDebitOperationCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getDirectDebitOperationCodeExportCFONB() == null || cfonbConfig.getDirectDebitOperationCodeExportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code opération Prélèvement CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	/******************************** IMPORT CFONB ********************************************/
	
	public void getHeaderRecordCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getHeaderRecordCodeImportCFONB() == null || cfonbConfig.getHeaderRecordCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code enregistrement en-tête CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getDetailRecordCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getDetailRecordCodeImportCFONB() == null || cfonbConfig.getDetailRecordCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code enregistrement detail CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getEndingRecordCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getEndingRecordCodeImportCFONB() == null || cfonbConfig.getEndingRecordCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code enregistrement fin CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getTransferOperationCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getTransferOperationCodeImportCFONB() == null || cfonbConfig.getTransferOperationCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code opération Virement rejeté CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getDirectDebitOperationCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getDirectDebitOperationCodeImportCFONB() == null || cfonbConfig.getDirectDebitOperationCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code opération Prélèvement impayé CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getIpoRejectOperationCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getIpoRejectOperationCodeImportCFONB() == null || cfonbConfig.getIpoRejectOperationCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code opération TIP impayé CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getIpoAndChequeOperationCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getIpoAndChequeOperationCodeImportCFONB() == null || cfonbConfig.getIpoAndChequeOperationCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code opération TIP + chèque CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getIpoOperationCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException  {
		
		if(cfonbConfig.getIpoOperationCodeImportCFONB() == null || cfonbConfig.getIpoOperationCodeImportCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code opération TIP CFONB pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),cfonbConfig.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	
	
}
