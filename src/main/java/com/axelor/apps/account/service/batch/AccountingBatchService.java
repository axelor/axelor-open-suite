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
package com.axelor.apps.account.service.batch;

import javax.inject.Inject;
import javax.inject.Provider;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.IAccount;
import com.axelor.apps.base.db.Batch;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

/**
 * InvoiceBatchService est une classe implémentant l'ensemble des batchs de
 * comptabilité et assimilé.
 * 
 * @author Geoffrey DUBAUX
 * 
 * @version 0.1
 */
public class AccountingBatchService {

	@Inject
	private Provider<BatchReminder> reminderProvider;
	
	@Inject
	private Provider<BatchDoubtfulCustomer> doubtfulCustomerProvider;
	
	@Inject
	private Provider<BatchReimbursementExport> reimbursementExportProvider;
	
	@Inject
	private Provider<BatchReimbursementImport> reimbursementImportProvider;
	
	@Inject
	private Provider<BatchPaymentScheduleExport> paymentScheduleExportProvider;
	
	@Inject
	private Provider<BatchPaymentScheduleImport> paymentScheduleImportProvider;
	
	@Inject
	private Provider<BatchInterbankPaymentOrderImport> interbankPaymentOrderImportProvider;
	
	@Inject
	private Provider<BatchInterbankPaymentOrderRejectImport> interbankPaymentOrderRejectImportProvider;
	
	@Inject
	private Provider<BatchAccountCustomer> accountCustomerProvider;
	
	@Inject
	private Provider<BatchMoveLineExport> moveLineExportProvider;

// Appel 	
	
	/**
	 * Lancer un batch à partir de son code.
	 * 
	 * @param batchCode
	 * 		Le code du batch souhaité.
	 * 
	 * @throws AxelorException
	 */
	public Batch run(String batchCode) throws AxelorException {
				
		Batch batch;
		AccountingBatch accountingBatch = AccountingBatch.all().filter("code = ?1", batchCode).fetchOne();
		
		if (accountingBatch != null){
			switch (accountingBatch.getActionSelect()) {
			case IAccount.BATCH_REIMBURSEMENT:
				if(accountingBatch.getReimbursementTypeSelect() == IAccount.BATCH_REIMBURSEMENT_EXPORT)  {
					batch = reimbursementExport(accountingBatch);
				}
				else if(accountingBatch.getReimbursementTypeSelect() == IAccount.BATCH_REIMBURSEMENT_IMPORT)  {
					batch = reimbursementImport(accountingBatch);
				}
				batch = null;
				break;
			case IAccount.BATCH_DIRECT_DEBIT:
				if(accountingBatch.getDirectDebitTypeSelect() == IAccount.BATCH_DIRECT_DEBIT_EXPORT)  {
					batch = paymentScheduleExport(accountingBatch);
				}
				else if(accountingBatch.getDirectDebitTypeSelect() == IAccount.BATCH_DIRECT_DEBIT_IMPORT)  {
					batch = paymentScheduleImport(accountingBatch);
				}
				batch = null;
				break;
			case IAccount.BATCH_REMINDER:
				batch = reminder(accountingBatch);
				break;
			case IAccount.BATCH_INTERBANK_PAYMENT_ORDER:
				if(accountingBatch.getInterbankPaymentOrderTypeSelect() == IAccount.INTERBANK_PAYMENT_ORDER_IMPORT)  {
					batch = interbankPaymentOrderImport(accountingBatch);
				}
				else if(accountingBatch.getInterbankPaymentOrderTypeSelect() == IAccount.INTERBANK_PAYMENT_ORDER_REJECT_IMPORT)  {
					batch = interbankPaymentOrderRejectImport(accountingBatch);
				}
				batch = null;
				break;
			case IAccount.BATCH_DOUBTFUL_CUSTOMER:
				batch = doubtfulCustomer(accountingBatch);
				break;
			case IAccount.BATCH_ACCOUNT_CUSTOMER:
				batch = accountCustomer(accountingBatch);
				break;
			case IAccount.BATCH_MOVE_LINE_EXPORT:
				batch = moveLineExport(accountingBatch);
				break;
			default:
				throw new AxelorException(String.format("Action %s inconnu pour le traitement %s", accountingBatch.getActionSelect(), batchCode), IException.INCONSISTENCY);
			}
		}
		else {
			throw new AxelorException(String.format("Batch %s inconnu", batchCode), IException.INCONSISTENCY);
		}
		
		return batch;
	}
	
	
	public Batch reminder(AccountingBatch accountingBatch) {
		
		BatchStrategy strategy = reminderProvider.get(); 
		return strategy.run(accountingBatch);
		
	}

	
	public Batch doubtfulCustomer(AccountingBatch accountingBatch) {
		
		BatchStrategy strategy = doubtfulCustomerProvider.get();
		return strategy.run(accountingBatch);
		
	}
	
	public Batch reimbursementExport(AccountingBatch accountingBatch) {
		
		BatchStrategy strategy = reimbursementExportProvider.get();
		return strategy.run(accountingBatch);
		
	}
	
	public Batch reimbursementImport(AccountingBatch accountingBatch) {
		
		BatchStrategy strategy = reimbursementImportProvider.get();
		return strategy.run(accountingBatch);
		
	}
	
	public Batch paymentScheduleExport(AccountingBatch accountingBatch) {
		
		BatchStrategy strategy = paymentScheduleExportProvider.get();
		return strategy.run(accountingBatch);
		
	}
	
	public Batch paymentScheduleImport(AccountingBatch accountingBatch) {
		
		BatchStrategy strategy = paymentScheduleImportProvider.get();
		return strategy.run(accountingBatch);
		
	}
	
	public Batch interbankPaymentOrderImport(AccountingBatch accountingBatch) {
		
		BatchStrategy strategy = interbankPaymentOrderImportProvider.get();
		return strategy.run(accountingBatch);
		
	}
	
	public Batch interbankPaymentOrderRejectImport(AccountingBatch accountingBatch) {
		
		BatchStrategy strategy = interbankPaymentOrderRejectImportProvider.get();
		return strategy.run(accountingBatch);
		
	}
	
	public Batch accountCustomer(AccountingBatch accountingBatch) {

		BatchStrategy strategy = accountCustomerProvider.get();
		return strategy.run(accountingBatch);

	}

	public Batch moveLineExport(AccountingBatch accountingBatch) {

		BatchStrategy strategy = moveLineExportProvider.get();
		return strategy.run(accountingBatch);

	}
	
}
