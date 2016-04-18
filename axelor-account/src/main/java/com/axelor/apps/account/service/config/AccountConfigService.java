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

import java.util.List;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.ReminderConfigLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.message.db.Template;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;

public class AccountConfigService {


	public AccountConfig getAccountConfig(Company company) throws AxelorException  {

		AccountConfig accountConfig = company.getAccountConfig();

		if(accountConfig == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_1),
					GeneralServiceImpl.EXCEPTION,company.getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig;

	}


	/******************************** EXPORT CFONB ********************************************/

	public void getReimbursementExportFolderPathCFONB(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getReimbursementExportFolderPathCFONB() == null || accountConfig.getReimbursementExportFolderPathCFONB().isEmpty())  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_2),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

	}

	public void getPaymentScheduleExportFolderPathCFONB(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getPaymentScheduleExportFolderPathCFONB() == null || accountConfig.getPaymentScheduleExportFolderPathCFONB().isEmpty())  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_3),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

	}


	/******************************** IMPORT CFONB ********************************************/


	public void getInterbankPaymentOrderImportPathCFONB(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getInterbankPaymentOrderImportPathCFONB() == null || accountConfig.getInterbankPaymentOrderImportPathCFONB().isEmpty())  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_4),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

	}

	public void getTempInterbankPaymentOrderImportPathCFONB(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getTempInterbankPaymentOrderImportPathCFONB() == null || accountConfig.getTempInterbankPaymentOrderImportPathCFONB().isEmpty())  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_5),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

	}

	public void getInterbankPaymentOrderRejectImportPathCFONB(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getInterbankPaymentOrderRejectImportPathCFONB() == null || accountConfig.getInterbankPaymentOrderRejectImportPathCFONB().isEmpty())  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_6),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

	}

	public void getTempInterbankPaymentOrderRejectImportPathCFONB(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getTempInterbankPaymentOrderRejectImportPathCFONB() == null || accountConfig.getTempInterbankPaymentOrderRejectImportPathCFONB().isEmpty())  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_7),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

	}

	public void getRejectImportPathAndFileName(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getRejectImportPathAndFileName() == null || accountConfig.getRejectImportPathAndFileName().isEmpty())  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_8),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

	}

	public void getTempImportPathAndFileName(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getTempImportPathAndFileName() == null || accountConfig.getTempImportPathAndFileName().isEmpty())  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_9),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

	}

	public void getReimbursementImportFolderPathCFONB(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getReimbursementImportFolderPathCFONB() == null || accountConfig.getReimbursementImportFolderPathCFONB().isEmpty())  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_10),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

	}

	public void getTempReimbImportFolderPathCFONB(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getTempReimbImportFolderPathCFONB() == null || accountConfig.getTempReimbImportFolderPathCFONB().isEmpty())  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_11),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

	}



	/******************************** JOURNAL ********************************************/


	public Journal getRejectJournal(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getRejectJournal() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_12),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getRejectJournal();
	}

	public Journal getIrrecoverableJournal(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getIrrecoverableJournal() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_13),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getIrrecoverableJournal();
	}

	public Journal getSupplierPurchaseJournal(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getSupplierPurchaseJournal() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_14),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getSupplierPurchaseJournal();
	}

	public Journal getSupplierCreditNoteJournal(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getSupplierCreditNoteJournal() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_15),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getSupplierCreditNoteJournal();
	}

	public Journal getCustomerSalesJournal(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getCustomerSalesJournal() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_16),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getCustomerSalesJournal();
	}

	public Journal getCustomerCreditNoteJournal(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getCustomerCreditNoteJournal() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_17),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getCustomerCreditNoteJournal();
	}

	public Journal getMiscOperationJournal(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getMiscOperationJournal() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_18),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getMiscOperationJournal();
	}

	public Journal getReimbursementJournal(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getReimbursementJournal() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_19),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getReimbursementJournal();
	}



	/******************************** JOURNAL TYPE ********************************************/


	public JournalType getSaleJournalType(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getSaleJournalType() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_20),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getSaleJournalType();
	}

	public JournalType getCreditNoteJournalType(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getCreditNoteJournalType() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_21),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getCreditNoteJournalType();
	}

	public JournalType getCashJournalType(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getCashJournalType() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_22),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getCashJournalType();
	}

	public JournalType getPurchaseJournalType(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getPurchaseJournalType() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_23),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getPurchaseJournalType();
	}



	/******************************** ACCOUNT ********************************************/


	public Account getIrrecoverableAccount(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getIrrecoverableAccount() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_24),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getIrrecoverableAccount();
	}

	public Account getCustomerAccount(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getCustomerAccount() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_25),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getCustomerAccount();

	}

	public Account getSupplierAccount(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getSupplierAccount() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_26),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getSupplierAccount();

	}
	
	public Account getAdvancePaymentAccount(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getAdvancePaymentAccount() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_38),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getAdvancePaymentAccount();

	}

	public Account getCashPositionVariationAccount(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getCashPositionVariationAccount() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_27),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getCashPositionVariationAccount();

	}

	public Account getReimbursementAccount(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getReimbursementAccount() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_28),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getReimbursementAccount();

	}

	public Account getDoubtfulCustomerAccount(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getDoubtfulCustomerAccount() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_29),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getDoubtfulCustomerAccount();

	}


	/******************************** TVA ********************************************/

	public Tax getIrrecoverableStandardRateTax(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getIrrecoverableStandardRateTax() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CLEARANCE_3),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getIrrecoverableStandardRateTax();
	}

	/******************************** PAYMENT MODE ********************************************/

	public PaymentMode getDirectDebitPaymentMode(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getDirectDebitPaymentMode() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_30),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getDirectDebitPaymentMode();
	}

	public PaymentMode getRejectionPaymentMode(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getRejectionPaymentMode() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_31),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getRejectionPaymentMode();
	}


	/******************************** OTHER ********************************************/

	public String getIrrecoverableReasonPassage(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getIrrecoverableReasonPassage() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_32),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getIrrecoverableReasonPassage();

	}

	public String getExportPath(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getExportPath() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_33),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getExportPath();

	}

	public Template getRejectPaymentScheduleTemplate(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getRejectPaymentScheduleTemplate() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_34),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getRejectPaymentScheduleTemplate();

	}

	public String getReimbursementExportFolderPath(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getReimbursementExportFolderPath() == null)   {
			throw new AxelorException(String.format("%s :\n "+I18n.get(IExceptionMessage.REIMBURSEMENT_2),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getReimbursementExportFolderPath();

	}

	public String getSixMonthDebtPassReason(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getSixMonthDebtPassReason() == null || accountConfig.getSixMonthDebtPassReason().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_35),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getSixMonthDebtPassReason();

	}

	public String getThreeMonthDebtPassReason(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getThreeMonthDebtPassReason() == null || accountConfig.getThreeMonthDebtPassReason().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_36),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getThreeMonthDebtPassReason();

	}

	public List<ReminderConfigLine> getReminderConfigLineList(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getReminderConfigLineList() == null || accountConfig.getReminderConfigLineList().isEmpty())   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_37),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getReminderConfigLineList();

	}

	/******************************** Sequence ********************************************/

	public Sequence getCustInvSequence(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getCustInvSequence() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_1),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getCustInvSequence();

	}

	public Sequence getCustRefSequence(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getCustRefSequence() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_2),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getCustRefSequence();

	}

	public Sequence getSuppInvSequence(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getSuppInvSequence() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_3),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getSuppInvSequence();

	}

	public Sequence getSuppRefSequence(AccountConfig accountConfig) throws AxelorException  {

		if(accountConfig.getSuppRefSequence() == null)   {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_4),
					GeneralServiceImpl.EXCEPTION,accountConfig.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}

		return accountConfig.getSuppRefSequence();

	}
	
	
	
	
	public boolean getInvoiceInAti(AccountConfig accountConfig) throws AxelorException  {
		
		int atiChoice = accountConfig.getInvoiceInAtiSelect();
		
		if(atiChoice == AccountConfigRepository.INVOICE_ATI_DEFAULT || atiChoice == AccountConfigRepository.INVOICE_ATI_ALWAYS)  {
			return true;
		}
		return false;
	}
	


}
