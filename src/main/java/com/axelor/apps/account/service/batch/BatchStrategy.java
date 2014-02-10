/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLineReport;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.CfonbService;
import com.axelor.apps.account.service.InterbankPaymentOrderImportService;
import com.axelor.apps.account.service.InterbankPaymentOrderRejectImportService;
import com.axelor.apps.account.service.MoveLineExportService;
import com.axelor.apps.account.service.PaymentScheduleExportService;
import com.axelor.apps.account.service.PaymentScheduleImportService;
import com.axelor.apps.account.service.ReimbursementExportService;
import com.axelor.apps.account.service.ReimbursementImportService;
import com.axelor.apps.account.service.RejectImportService;
import com.axelor.apps.account.service.debtrecovery.DoubtfulCustomerService;
import com.axelor.apps.account.service.debtrecovery.ReminderService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.MailService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public abstract class BatchStrategy extends AbstractBatch {

	protected ReminderService reminderService;
	protected MailService mailService;
	protected DoubtfulCustomerService doubtfulCustomerService;
	protected ReimbursementExportService reimbursementExportService;
	protected ReimbursementImportService reimbursementImportService;
	protected RejectImportService rejectImportService;
	protected CfonbService cfonbService;
	protected PaymentScheduleExportService paymentScheduleExportService;
	protected PaymentScheduleImportService paymentScheduleImportService;
	protected PaymentModeService paymentModeService;
	protected InterbankPaymentOrderImportService interbankPaymentOrderImportService;
	protected InterbankPaymentOrderRejectImportService interbankPaymentOrderRejectImportService;
	protected AccountCustomerService accountCustomerService;
	protected MoveLineExportService moveLineExportService;
	protected BatchAccountCustomer batchAccountCustomer;


	
	protected BatchStrategy(ReminderService reminderService, MailService mailService) {
		super();
		this.reminderService = reminderService;
		this.mailService = mailService;
	}
	
	protected BatchStrategy(DoubtfulCustomerService doubtfulCustomerService, BatchAccountCustomer batchAccountCustomer) {
		super();
		this.doubtfulCustomerService = doubtfulCustomerService;
		this.batchAccountCustomer = batchAccountCustomer;
	}
	
	protected BatchStrategy(ReimbursementExportService reimbursementExportService, CfonbService cfonbService, BatchAccountCustomer batchAccountCustomer) {
		super();
		this.reimbursementExportService = reimbursementExportService;
		this.cfonbService = cfonbService;
		this.batchAccountCustomer = batchAccountCustomer;
	}
	
	protected BatchStrategy(ReimbursementImportService reimbursementImportService, RejectImportService rejectImportService, BatchAccountCustomer batchAccountCustomer) {
		super();
		this.reimbursementImportService = reimbursementImportService;
		this.rejectImportService = rejectImportService;
		this.batchAccountCustomer = batchAccountCustomer;
	}
	
	
	
	protected BatchStrategy(PaymentScheduleExportService paymentScheduleExportService, PaymentModeService paymentModeService, CfonbService cfonbService, BatchAccountCustomer batchAccountCustomer) {
		super();
		this.paymentScheduleExportService = paymentScheduleExportService;
		this.cfonbService = cfonbService;
		this.paymentModeService = paymentModeService;
		this.batchAccountCustomer = batchAccountCustomer;
	}
	
	protected BatchStrategy(PaymentScheduleImportService paymentScheduleImportService, RejectImportService rejectImportService, BatchAccountCustomer batchAccountCustomer) {
		super();
		this.paymentScheduleImportService = paymentScheduleImportService;
		this.rejectImportService = rejectImportService;
		this.batchAccountCustomer = batchAccountCustomer;
	}
	
	protected BatchStrategy(InterbankPaymentOrderImportService interbankPaymentOrderImportService, CfonbService cfonbService, RejectImportService rejectImportService, BatchAccountCustomer batchAccountCustomer) {
		super();
		this.interbankPaymentOrderImportService = interbankPaymentOrderImportService;
		this.cfonbService = cfonbService;
		this.rejectImportService = rejectImportService;
		this.batchAccountCustomer = batchAccountCustomer;
	}
	
	protected BatchStrategy(InterbankPaymentOrderRejectImportService interbankPaymentOrderRejectImportService, CfonbService cfonbService, RejectImportService rejectImportService, BatchAccountCustomer batchAccountCustomer) {
		super();
		this.interbankPaymentOrderRejectImportService = interbankPaymentOrderRejectImportService;
		this.cfonbService = cfonbService;
		this.rejectImportService = rejectImportService;
		this.batchAccountCustomer = batchAccountCustomer;
	}
	
	protected BatchStrategy(AccountCustomerService accountCustomerService) {
		super();
		this.accountCustomerService = accountCustomerService;
	}
	
	protected BatchStrategy(MoveLineExportService moveLineExportService) {
		super();
		this.moveLineExportService = moveLineExportService;
	}
	
	protected void updateInvoice( Invoice invoice ){
		
		invoice.addBatchSetItem( Batch.find( batch.getId() ) );
			
		incrementDone();
	}
	
	protected void updateReimbursement( Reimbursement reimbursement ){
		
		reimbursement.addBatchSetItem( Batch.find( batch.getId() ) );
			
		incrementDone();
	}
	
	protected void updatePaymentScheduleLine( PaymentScheduleLine paymentScheduleLine ){
		
		paymentScheduleLine.addBatchSetItem( Batch.find( batch.getId() ) );
			
		incrementDone();
	}
	
	protected void updatePaymentVoucher( PaymentVoucher paymentVoucher ){
		
		paymentVoucher.addBatchSetItem( Batch.find( batch.getId() ) );
			
		incrementDone();
	}
	
	protected void updatePartner( Partner partner ){
		
		partner.addBatchSetItem( Batch.find( batch.getId() ) );
			
		incrementDone();
	}
	
	protected void updateAccountingSituation( AccountingSituation accountingSituation ){
		
		accountingSituation.addBatchSetItem( Batch.find( batch.getId() ) );
			
		incrementDone();
	}
	
	protected void updateMoveLineReport( MoveLineReport moveLineReport){
		
		moveLineReport.addBatchSetItem( Batch.find( batch.getId() ) );
			
		incrementDone();
	}
	
	public void testAccountingBatchBankDetails(AccountingBatch accountingBatch) throws AxelorException  {
		
		if(accountingBatch.getBankDetails() == null) {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un RIB pour le configurateur de batch %s",
					GeneralService.getExceptionAccountingMsg(),accountingBatch.getCode()), IException.CONFIGURATION_ERROR);
		}
		
		this.cfonbService.testBankDetailsField(accountingBatch.getBankDetails());
		
	}
	
}
