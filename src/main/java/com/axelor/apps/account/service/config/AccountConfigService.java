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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.Vat;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.MailModel;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class AccountConfigService {
	
	private static final Logger LOG = LoggerFactory.getLogger(AccountConfigService.class);

	
	public AccountConfig getAccountConfig(Company company) throws AxelorException  {
		
		AccountConfig accountConfig = company.getAccountConfig();
		
		if(accountConfig == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer les informations comptables pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig;
		
	}
	
	
	/******************************** EXPORT CFONB ********************************************/
	
	public void getReimbursementExportFolderPathCFONB(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getReimbursementExportFolderPathCFONB() == null || accountConfig.getReimbursementExportFolderPathCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Dossier d'export des remboursements au format CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getPaymentScheduleExportFolderPathCFONB(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getPaymentScheduleExportFolderPathCFONB() == null || accountConfig.getPaymentScheduleExportFolderPathCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Dossier d'export des prélèvements au format CFONB pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	
	/******************************** IMPORT CFONB ********************************************/
	
	
	public void getInterbankPaymentOrderImportPathCFONB(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getInterbankPaymentOrderImportPathCFONB() == null || accountConfig.getInterbankPaymentOrderImportPathCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un chemin d'import des paiements par TIP et TIP + chèque pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getTempInterbankPaymentOrderImportPathCFONB(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getTempInterbankPaymentOrderImportPathCFONB() == null || accountConfig.getTempInterbankPaymentOrderImportPathCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un chemin d'import temporaire des paiements par TIP et TIP + chèque pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getInterbankPaymentOrderRejectImportPathCFONB(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getInterbankPaymentOrderRejectImportPathCFONB() == null || accountConfig.getInterbankPaymentOrderRejectImportPathCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un chemin pour le fichier d'imports des rejets de paiement par TIP et TIP chèque pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getTempInterbankPaymentOrderRejectImportPathCFONB(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getTempInterbankPaymentOrderRejectImportPathCFONB() == null || accountConfig.getTempInterbankPaymentOrderRejectImportPathCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un chemin pour le fichier des rejets de paiement par TIP et TIP chèque temporaire pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getRejectImportPathAndFileName(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getRejectImportPathAndFileName() == null || accountConfig.getRejectImportPathAndFileName().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un chemin pour le fichier de rejet pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getTempImportPathAndFileName(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getTempImportPathAndFileName() == null || accountConfig.getTempImportPathAndFileName().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un chemin pour le fichier de rejet temporaire pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getReimbursementImportFolderPathCFONB(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getReimbursementImportFolderPathCFONB() == null || accountConfig.getReimbursementImportFolderPathCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un chemin pour le fichier d'imports des rejets des remboursements pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	public void getTempReimbImportFolderPathCFONB(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getTempReimbImportFolderPathCFONB() == null || accountConfig.getTempReimbImportFolderPathCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un chemin pour le fichier temporaire d'imports des rejets des remboursements pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	
	
	/******************************** JOURNAL ********************************************/
	
	
	public Journal getRejectJournal(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getRejectJournal() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un journal de rejet pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getRejectJournal();
	}
	
	public Journal getIrrecoverableJournal(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getIrrecoverableJournal() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un journal irrécouvrable pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getIrrecoverableJournal();
	}
	
	public Journal getSupplierPurchaseJournal(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getSupplierPurchaseJournal() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un journal des achats Fourn. pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getSupplierPurchaseJournal();
	}
	
	public Journal getSupplierCreditNoteJournal(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getSupplierCreditNoteJournal() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un journal des avoirs fournisseurs pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getSupplierCreditNoteJournal();
	}

	public Journal getCustomerSalesJournal(AccountConfig accountConfig) throws AxelorException  {
	
		if(accountConfig.getCustomerSalesJournal() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un journal des ventes clients pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getCustomerSalesJournal();
	}
	
	public Journal getCustomerCreditNoteJournal(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getCustomerCreditNoteJournal() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un journal des avoirs clients pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getCustomerCreditNoteJournal();
	}
	
	public Journal getMiscOperationJournal(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getMiscOperationJournal() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un journal des O.D pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getMiscOperationJournal();
	}
	
	
	public Journal getInvoiceDirectDebitJournal(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getInvoiceDirectDebitJournal() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un journal prélèvement facture pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getInvoiceDirectDebitJournal();
	}
	
	
	public Journal getScheduleDirectDebitJournal(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getScheduleDirectDebitJournal() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un journal prélèvement échéancier pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getScheduleDirectDebitJournal();
	}
	
	public Journal getReimbursementJournal(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getReimbursementJournal() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un journal de remboursement pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getReimbursementJournal();
	}
	
	
	
	/******************************** JOURNAL TYPE ********************************************/
	
	
	public JournalType getSaleJournalType(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getSaleJournalType() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un type de journal ventes pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getSaleJournalType();
	}
	
	public JournalType getCreditNoteJournalType(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getCreditNoteJournalType() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un type de journal avoirs pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getCreditNoteJournalType();
	}
	
	public JournalType getCashJournalType(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getCashJournalType() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un type de journal trésorerie pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getCashJournalType();
	}
	
	public JournalType getPurchaseJournalType(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getPurchaseJournalType() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un type de journal achats pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getPurchaseJournalType();
	}
	
	
	
	/******************************** ACCOUNT ********************************************/
	
	
	public Account getIrrecoverableAccount(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getIrrecoverableAccount() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un compte de créance irrécouvrable pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getIrrecoverableAccount();
	}
	
	public Account getCustomerAccount(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getCustomerAccount() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un compte client pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getCustomerAccount();
		
	}
	
	public Account getSupplierAccount(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getSupplierAccount() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un compte fournisseur pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getCustomerAccount();
		
	}
	
	public Account getCashPositionVariationAccount(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getCashPositionVariationAccount() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un compte différence de caisse pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getCashPositionVariationAccount();
		
	}
	
	public Account getReimbursementAccount(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getReimbursementAccount() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un compte de remboursement pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getReimbursementAccount();
		
	}
	
	public Account getDoubtfulCustomerAccount(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getDoubtfulCustomerAccount() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un compte client douteux pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getDoubtfulCustomerAccount();
		
	}
	
	
	/******************************** TVA ********************************************/
	
	public Vat getIrrecoverableStandardRateVat(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getIrrecoverableStandardRateVat() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une TVA taux normal pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getIrrecoverableStandardRateVat();
	}
	
	/******************************** PAYMENT MODE ********************************************/
	
	public PaymentMode getDirectDebitPaymentMode(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getDirectDebitPaymentMode() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un mode de paiement par prélèvement pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getDirectDebitPaymentMode();
	}
	
	public PaymentMode getRejectionPaymentMode(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getRejectionPaymentMode() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un mode de paiement après rejet pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getRejectionPaymentMode();
	}
	
	
	/******************************** OTHER ********************************************/
	
	public String getIrrecoverableReasonPassage(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getIrrecoverableReasonPassage() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un motif de passage en irrécouvrable pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getIrrecoverableReasonPassage();
		
	}
	
	public String getExportPath(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getExportPath() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Chemin Fichier Exporté (si -> AGRESSO) pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getExportPath();
		
	}
	
	public MailModel getRejectPaymentScheduleMailModel(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getRejectPaymentScheduleMailModel() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer les modèles de courrier Imports de rejet pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getRejectPaymentScheduleMailModel();
		
	}
	
	public String getReimbursementExportFolderPath(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getReimbursementExportFolderPath() == null)   {
			throw new AxelorException(String.format("%s :\n Le dossier d'export des remboursement (format SEPA) n'est pas configuré pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getReimbursementExportFolderPath();
		
	}
	
	public String getSixMonthDebtPassReason(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getSixMonthDebtPassReason() == null || accountConfig.getSixMonthDebtPassReason().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Motif de passage (créance de plus de six mois) pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getSixMonthDebtPassReason();
		
	}
	
	public String getThreeMonthDebtPassReason(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getThreeMonthDebtPassReason() == null || accountConfig.getThreeMonthDebtPassReason().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Motif de passage (créance de plus de trois mois) pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountConfig.getThreeMonthDebtPassReason();
		
	}
	
	public void getReminderConfigLineList(AccountConfig accountConfig) throws AxelorException  {
		
		if(accountConfig.getReminderConfigLineList() == null || accountConfig.getReminderConfigLineList().isEmpty())   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer le tableau de relance pour la société %s",
					GeneralService.getExceptionAccountingMsg(),accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	
}
