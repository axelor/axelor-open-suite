/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.batch;

import javax.inject.Inject;
import javax.inject.Provider;

import com.axelor.apps.account.db.AccountingBatch;
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
		AccountingBatch accountingBatch = AccountingBatch.findByCode(batchCode);
		
		if (accountingBatch != null){
			switch (accountingBatch.getActionSelect()) {
			case AccountingBatch.ACTION_REIMBURSEMENT:
				if(accountingBatch.getReimbursementTypeSelect() == AccountingBatch.REIMBURSEMENT_TYPE_EXPORT)  {
					batch = reimbursementExport(accountingBatch);
				}
				else if(accountingBatch.getReimbursementTypeSelect() == AccountingBatch.REIMBURSEMENT_TYPE_IMPORT)  {
					batch = reimbursementImport(accountingBatch);
				}
				batch = null;
				break;
			case AccountingBatch.ACTION_DIRECT_DEBIT:
				if(accountingBatch.getDirectDebitTypeSelect() == AccountingBatch.DIRECT_DEBIT_TYPE_EXPORT)  {
					batch = paymentScheduleExport(accountingBatch);
				}
				else if(accountingBatch.getDirectDebitTypeSelect() == AccountingBatch.DIRECT_DEBIT_TYPE_IMPORT)  {
					batch = paymentScheduleImport(accountingBatch);
				}
				batch = null;
				break;
			case AccountingBatch.ACTION_REMINDER:
				batch = reminder(accountingBatch);
				break;
			case AccountingBatch.ACTION_INTERBANK_PAYMENT_ORDER:
				if(accountingBatch.getInterbankPaymentOrderTypeSelect() == AccountingBatch.INTERBANK_PAYMENT_ORDER_TYPE_IMPORT)  {
					batch = interbankPaymentOrderImport(accountingBatch);
				}
				else if(accountingBatch.getInterbankPaymentOrderTypeSelect() == AccountingBatch.INTERBANK_PAYMENT_ORDER_TYPE_REJECT_IMPORT)  {
					batch = interbankPaymentOrderRejectImport(accountingBatch);
				}
				batch = null;
				break;
			case AccountingBatch.ACTION_DOUBTFUL_CUSTOMER:
				batch = doubtfulCustomer(accountingBatch);
				break;
			case AccountingBatch.ACTION_ACCOUNT_CUSTOMER:
				batch = accountCustomer(accountingBatch);
				break;
			case AccountingBatch.ACTION_MOVE_LINE_EXPORT:
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
