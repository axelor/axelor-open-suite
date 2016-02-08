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
package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.service.batch.AccountingBatchService;
import com.axelor.apps.base.db.Batch;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AccountingBatchController {

	@Inject
	private AccountingBatchService accountingBatchService;
	
	@Inject
	private AccountingBatchRepository accountingBatchRepo;

	/**
	 * Lancer le batch de relance
	 *
	 * @param request
	 * @param response
	 */
	public void actionReminder(ActionRequest request, ActionResponse response){

		AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);

		Batch batch = null;

		if(accountingBatch.getReminderTypeSelect() == AccountingBatchRepository.ACTION_REMINDER)  {
			batch = accountingBatchService.reminder(accountingBatchRepo.find(accountingBatch.getId()));
		}
		if(batch != null)
			response.setFlash(batch.getComments());
		response.setReload(true);
	}

	/**
	 * Lancer le batch de détermination des créances douteuses
	 *
	 * @param request
	 * @param response
	 */
	public void actionDoubtfulCustomer(ActionRequest request, ActionResponse response){

		AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);

		Batch batch = null;

		batch = accountingBatchService.doubtfulCustomer(accountingBatchRepo.find(accountingBatch.getId()));

		if(batch != null)
			response.setFlash(batch.getComments());
		response.setReload(true);
	}

	/**
	 * Lancer le batch de remboursement
	 *
	 * @param request
	 * @param response
	 */
	public void actionReimbursement(ActionRequest request, ActionResponse response){

		AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);

		Batch batch = null;

		if(accountingBatch.getReimbursementTypeSelect() == AccountingBatchRepository.REIMBURSEMENT_TYPE_EXPORT)  {
			batch = accountingBatchService.reimbursementExport(accountingBatchRepo.find(accountingBatch.getId()));
		}
		else if(accountingBatch.getReimbursementTypeSelect() == AccountingBatchRepository.REIMBURSEMENT_TYPE_IMPORT)  {
			batch = accountingBatchService.reimbursementImport(accountingBatchRepo.find(accountingBatch.getId()));
		}

		if(batch != null)
			response.setFlash(batch.getComments());
		response.setReload(true);
	}

	/**
	 * Lancer le batch de prélèvement
	 *
	 * @param request
	 * @param response
	 */
	public void actionDirectDebit(ActionRequest request, ActionResponse response){

		AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);

		Batch batch = null;

		if(accountingBatch.getDirectDebitTypeSelect() == AccountingBatchRepository.DIRECT_DEBIT_TYPE_EXPORT)  {
			batch = accountingBatchService.paymentScheduleExport(accountingBatchRepo.find(accountingBatch.getId()));
		}
		else if(accountingBatch.getDirectDebitTypeSelect() == AccountingBatchRepository.DIRECT_DEBIT_TYPE_IMPORT)  {
			batch = accountingBatchService.paymentScheduleImport(accountingBatchRepo.find(accountingBatch.getId()));
		}

		if(batch != null)
			response.setFlash(batch.getComments());
		response.setReload(true);
	}

	/**
	 * Lancer le batch de prélèvement
	 *
	 * @param request
	 * @param response
	 */
	public void actionInterbankPaymentOrder(ActionRequest request, ActionResponse response){

		AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);

		Batch batch = null;

		if(accountingBatch.getInterbankPaymentOrderTypeSelect() == AccountingBatchRepository.INTERBANK_PAYMENT_ORDER_TYPE_IMPORT)  {
			batch = accountingBatchService.interbankPaymentOrderImport(accountingBatchRepo.find(accountingBatch.getId()));
		}
		else if(accountingBatch.getInterbankPaymentOrderTypeSelect() == AccountingBatchRepository.INTERBANK_PAYMENT_ORDER_TYPE_REJECT_IMPORT)  {
			batch = accountingBatchService.interbankPaymentOrderRejectImport(accountingBatchRepo.find(accountingBatch.getId()));
		}

		if(batch != null)
			response.setFlash(batch.getComments());
		response.setReload(true);
	}


	/**
	 * Lancer le batch de calcul du compte client
	 *
	 * @param request
	 * @param response
	 */
	public void actionAccountingCustomer(ActionRequest request, ActionResponse response){

		AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);

		Batch batch = null;

		batch = accountingBatchService.accountCustomer(accountingBatchRepo.find(accountingBatch.getId()));

		if(batch != null)
			response.setFlash(batch.getComments());
		response.setReload(true);

	}



	/**
	 * Lancer le batch de calcul du compte client
	 *
	 * @param request
	 * @param response
	 */
	public void actionMoveLineExport(ActionRequest request, ActionResponse response){

		AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);

		Batch batch = null;

		batch = accountingBatchService.moveLineExport(accountingBatchRepo.find(accountingBatch.getId()));

		if(batch != null)
			response.setFlash(batch.getComments());
		response.setReload(true);

	}


	// WS

	/**
	 * Lancer le batch à travers un web service.
	 *
	 * @param request
	 * @param response
	 * @throws AxelorException
	 */
	public void run(ActionRequest request, ActionResponse response) throws AxelorException{

		Batch batch = accountingBatchService.run((String) request.getContext().get("code"));
	    Map<String,Object> mapData = new HashMap<String,Object>();
		mapData.put("anomaly", batch.getAnomaly());
		response.setData(mapData);
	}
}
