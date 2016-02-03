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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

/**
 * InvoiceBatchService est une classe implémentant l'ensemble des batchs de
 * comptabilité et assimilé.
 * 
 * @author Geoffrey DUBAUX
 * 
 * @version 0.1
 */
public class AccountingBatchService {

	AccountingBatchRepository accountingBatchRepo;
	
	@Inject
	public AccountingBatchService(AccountingBatchRepository accountingBatchRepo)  {
		
		this.accountingBatchRepo = accountingBatchRepo;
		
	}
	
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
		AccountingBatch accountingBatch = accountingBatchRepo.findByCode(batchCode);
		
		if (accountingBatch != null){
			switch (accountingBatch.getActionSelect()) {
			case AccountingBatchRepository.ACTION_REIMBURSEMENT:
				if(accountingBatch.getReimbursementTypeSelect() == AccountingBatchRepository.REIMBURSEMENT_TYPE_EXPORT)  {
					batch = reimbursementExport(accountingBatch);
				}
				else if(accountingBatch.getReimbursementTypeSelect() == 
						AccountingBatchRepository.REIMBURSEMENT_TYPE_IMPORT)  {
					batch = reimbursementImport(accountingBatch);
				}
				batch = null;
				break;
			case AccountingBatchRepository.ACTION_DIRECT_DEBIT:
				if(accountingBatch.getDirectDebitTypeSelect() == AccountingBatchRepository.DIRECT_DEBIT_TYPE_EXPORT)  {
					batch = paymentScheduleExport(accountingBatch);
				}
				else if(accountingBatch.getDirectDebitTypeSelect() == AccountingBatchRepository.DIRECT_DEBIT_TYPE_IMPORT)  {
					batch = paymentScheduleImport(accountingBatch);
				}
				batch = null;
				break;
			case AccountingBatchRepository.ACTION_REMINDER:
				batch = reminder(accountingBatch);
				break;
			case AccountingBatchRepository.ACTION_INTERBANK_PAYMENT_ORDER:
				if(accountingBatch.getInterbankPaymentOrderTypeSelect() == AccountingBatchRepository.INTERBANK_PAYMENT_ORDER_TYPE_IMPORT)  {
					batch = interbankPaymentOrderImport(accountingBatch);
				}
				else if(accountingBatch.getInterbankPaymentOrderTypeSelect() == AccountingBatchRepository.INTERBANK_PAYMENT_ORDER_TYPE_REJECT_IMPORT)  {
					batch = interbankPaymentOrderRejectImport(accountingBatch);
				}
				batch = null;
				break;
			case AccountingBatchRepository.ACTION_DOUBTFUL_CUSTOMER:
				batch = doubtfulCustomer(accountingBatch);
				break;
			case AccountingBatchRepository.ACTION_ACCOUNT_CUSTOMER:
				batch = accountCustomer(accountingBatch);
				break;
			case AccountingBatchRepository.ACTION_MOVE_LINE_EXPORT:
				batch = moveLineExport(accountingBatch);
				break;
			default:
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.BASE_BATCH_1), accountingBatch.getActionSelect(), batchCode), IException.INCONSISTENCY);
			}
		}
		else {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BASE_BATCH_2), batchCode), IException.INCONSISTENCY);
		}
		
		return batch;
	}
	
	
	public Batch reminder(AccountingBatch accountingBatch) {
		
		return Beans.get(BatchReminder.class).run(accountingBatch);
		
	}

	
	public Batch doubtfulCustomer(AccountingBatch accountingBatch) {
		
		return Beans.get(BatchDoubtfulCustomer.class).run(accountingBatch);
	}
	
	public Batch reimbursementExport(AccountingBatch accountingBatch) {
		
		return Beans.get(BatchReimbursementExport.class).run(accountingBatch);
	}
	
	public Batch reimbursementImport(AccountingBatch accountingBatch) {
		
		return Beans.get(BatchReimbursementImport.class).run(accountingBatch);
		
	}
	
	public Batch paymentScheduleExport(AccountingBatch accountingBatch) {
		
		return Beans.get(BatchPaymentScheduleExport.class).run(accountingBatch);
		
	}
	
	public Batch paymentScheduleImport(AccountingBatch accountingBatch) {
		
		return Beans.get(BatchPaymentScheduleImport.class).run(accountingBatch);
		
	}
	
	public Batch interbankPaymentOrderImport(AccountingBatch accountingBatch) {
		
		return Beans.get(BatchInterbankPaymentOrderImport.class).run(accountingBatch);
		
	}
	
	public Batch interbankPaymentOrderRejectImport(AccountingBatch accountingBatch) {
		
		return Beans.get(BatchInterbankPaymentOrderRejectImport.class).run(accountingBatch);
		
	}
	
	public Batch accountCustomer(AccountingBatch accountingBatch) {

		return Beans.get(BatchAccountCustomer.class).run(accountingBatch);

	}

	public Batch moveLineExport(AccountingBatch accountingBatch) {

		return Beans.get(BatchMoveLineExport.class).run(accountingBatch);

	}
	
}
