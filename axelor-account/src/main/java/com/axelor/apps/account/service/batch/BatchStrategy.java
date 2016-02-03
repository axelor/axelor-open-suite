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
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLineReport;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.InterbankPaymentOrderImportService;
import com.axelor.apps.account.service.InterbankPaymentOrderRejectImportService;
import com.axelor.apps.account.service.MoveLineExportService;
import com.axelor.apps.account.service.PaymentScheduleExportService;
import com.axelor.apps.account.service.PaymentScheduleImportService;
import com.axelor.apps.account.service.ReimbursementExportService;
import com.axelor.apps.account.service.ReimbursementImportService;
import com.axelor.apps.account.service.ReimbursementService;
import com.axelor.apps.account.service.RejectImportService;
import com.axelor.apps.account.service.cfonb.CfonbExportService;
import com.axelor.apps.account.service.cfonb.CfonbImportService;
import com.axelor.apps.account.service.debtrecovery.DoubtfulCustomerService;
import com.axelor.apps.account.service.debtrecovery.ReminderService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public abstract class BatchStrategy extends AbstractBatch {

	protected ReminderService reminderService;
	protected DoubtfulCustomerService doubtfulCustomerService;
	protected ReimbursementExportService reimbursementExportService;
	protected ReimbursementImportService reimbursementImportService;
	protected RejectImportService rejectImportService;
	protected CfonbExportService cfonbExportService;
	protected CfonbImportService cfonbImportService;
	protected PaymentScheduleExportService paymentScheduleExportService;
	protected PaymentScheduleImportService paymentScheduleImportService;
	protected PaymentModeService paymentModeService;
	protected InterbankPaymentOrderImportService interbankPaymentOrderImportService;
	protected InterbankPaymentOrderRejectImportService interbankPaymentOrderRejectImportService;
	protected AccountCustomerService accountCustomerService;
	protected MoveLineExportService moveLineExportService;
	protected BatchAccountCustomer batchAccountCustomer;

	@Inject
	protected BatchRepository batchRepo;

	@Inject
	protected CompanyRepository companyRepo;

	@Inject
	protected MoveService moveService;
	
	@Inject
	protected MoveRepository moveRepo;

	@Inject
	protected MoveLineService moveLineService;
	
	@Inject
	protected MoveLineRepository  moveLineRepo;

	@Inject
	protected ReimbursementService reimbursementService;


	protected BatchStrategy(ReminderService reminderService) {
		super();
		this.reminderService = reminderService;
	}

	protected BatchStrategy(DoubtfulCustomerService doubtfulCustomerService, BatchAccountCustomer batchAccountCustomer) {
		super();
		this.doubtfulCustomerService = doubtfulCustomerService;
		this.batchAccountCustomer = batchAccountCustomer;
	}

	protected BatchStrategy(ReimbursementExportService reimbursementExportService, CfonbExportService cfonbExportService, BatchAccountCustomer batchAccountCustomer) {
		super();
		this.reimbursementExportService = reimbursementExportService;
		this.cfonbExportService = cfonbExportService;
		this.batchAccountCustomer = batchAccountCustomer;
	}

	protected BatchStrategy(ReimbursementImportService reimbursementImportService, RejectImportService rejectImportService, BatchAccountCustomer batchAccountCustomer) {
		super();
		this.reimbursementImportService = reimbursementImportService;
		this.rejectImportService = rejectImportService;
		this.batchAccountCustomer = batchAccountCustomer;
	}



	protected BatchStrategy(PaymentScheduleExportService paymentScheduleExportService, PaymentModeService paymentModeService, CfonbExportService cfonbExportService, BatchAccountCustomer batchAccountCustomer) {
		super();
		this.paymentScheduleExportService = paymentScheduleExportService;
		this.cfonbExportService = cfonbExportService;
		this.paymentModeService = paymentModeService;
		this.batchAccountCustomer = batchAccountCustomer;
	}

	protected BatchStrategy(PaymentScheduleImportService paymentScheduleImportService, RejectImportService rejectImportService, BatchAccountCustomer batchAccountCustomer) {
		super();
		this.paymentScheduleImportService = paymentScheduleImportService;
		this.rejectImportService = rejectImportService;
		this.batchAccountCustomer = batchAccountCustomer;
	}

	protected BatchStrategy(InterbankPaymentOrderImportService interbankPaymentOrderImportService, CfonbImportService cfonbImportService, RejectImportService rejectImportService, BatchAccountCustomer batchAccountCustomer) {
		super();
		this.interbankPaymentOrderImportService = interbankPaymentOrderImportService;
		this.cfonbImportService = cfonbImportService;
		this.rejectImportService = rejectImportService;
		this.batchAccountCustomer = batchAccountCustomer;
	}

	protected BatchStrategy(InterbankPaymentOrderRejectImportService interbankPaymentOrderRejectImportService, RejectImportService rejectImportService, BatchAccountCustomer batchAccountCustomer) {
		super();
		this.interbankPaymentOrderRejectImportService = interbankPaymentOrderRejectImportService;
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

		invoice.addBatchSetItem( batchRepo.find( batch.getId() ) );

		incrementDone();
	}

	protected void updateReimbursement( Reimbursement reimbursement ){

		reimbursement.addBatchSetItem( batchRepo.find( batch.getId() ) );

		incrementDone();
	}

	protected void updatePaymentScheduleLine( PaymentScheduleLine paymentScheduleLine ){

		paymentScheduleLine.addBatchSetItem( batchRepo.find( batch.getId() ) );

		incrementDone();
	}

	protected void updatePaymentVoucher( PaymentVoucher paymentVoucher ){

		paymentVoucher.addBatchSetItem( batchRepo.find( batch.getId() ) );

		incrementDone();
	}

	protected void updatePartner( Partner partner ){

		partner.addBatchSetItem( batchRepo.find( batch.getId() ) );

		incrementDone();
	}

	protected void updateAccountingSituation( AccountingSituation accountingSituation ){

		accountingSituation.addBatchSetItem( batchRepo.find( batch.getId() ) );

		incrementDone();
	}

	protected void updateMoveLineReport( MoveLineReport moveLineReport){

		moveLineReport.addBatchSetItem( batchRepo.find( batch.getId() ) );

		incrementDone();
	}

	public void testAccountingBatchBankDetails(AccountingBatch accountingBatch) throws AxelorException  {

		if(accountingBatch.getBankDetails() == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_STRATEGY_1),
					GeneralServiceImpl.EXCEPTION,accountingBatch.getCode()), IException.CONFIGURATION_ERROR);
		}

		this.cfonbExportService.testBankDetailsField(accountingBatch.getBankDetails());

	}

}
