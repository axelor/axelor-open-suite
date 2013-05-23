package com.axelor.apps.account.service.generator.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.service.CfonbService;
import com.axelor.apps.account.service.InterbankPaymentOrderImportService;
import com.axelor.apps.account.service.InterbankPaymentOrderRejectImportService;
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


	
	protected BatchStrategy(ReminderService reminderService, MailService mailService) {
		super();
		this.reminderService = reminderService;
		this.mailService = mailService;
	}
	
	protected BatchStrategy(DoubtfulCustomerService doubtfulCustomerService) {
		super();
		this.doubtfulCustomerService = doubtfulCustomerService;
	}
	
	protected BatchStrategy(ReimbursementExportService reimbursementExportService, CfonbService cfonbService) {
		super();
		this.reimbursementExportService = reimbursementExportService;
		this.cfonbService = cfonbService;
	}
	
	protected BatchStrategy(ReimbursementImportService reimbursementImportService, RejectImportService rejectImportService) {
		super();
		this.reimbursementImportService = reimbursementImportService;
		this.rejectImportService = rejectImportService;
	}
	
	
	
	protected BatchStrategy(PaymentScheduleExportService paymentScheduleExportService, PaymentModeService paymentModeService, CfonbService cfonbService) {
		super();
		this.paymentScheduleExportService = paymentScheduleExportService;
		this.cfonbService = cfonbService;
		this.paymentModeService = paymentModeService;
	}
	
	protected BatchStrategy(PaymentScheduleImportService paymentScheduleImportService, RejectImportService rejectImportService) {
		super();
		this.paymentScheduleImportService = paymentScheduleImportService;
		this.rejectImportService = rejectImportService;
	}
	
	protected BatchStrategy(InterbankPaymentOrderImportService interbankPaymentOrderImportService, CfonbService cfonbService, RejectImportService rejectImportService) {
		super();
		this.interbankPaymentOrderImportService = interbankPaymentOrderImportService;
		this.cfonbService = cfonbService;
		this.rejectImportService = rejectImportService;
	}
	
	protected BatchStrategy(InterbankPaymentOrderRejectImportService interbankPaymentOrderRejectImportService, CfonbService cfonbService, RejectImportService rejectImportService) {
		super();
		this.interbankPaymentOrderRejectImportService = interbankPaymentOrderRejectImportService;
		this.cfonbService = cfonbService;
		this.rejectImportService = rejectImportService;
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
	
	public void testAccountingBatchBankDetails(AccountingBatch accountingBatch) throws AxelorException  {
		
		if(accountingBatch.getBankDetails() == null) {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un RIB pour le configurateur de batch %s",
					GeneralService.getExceptionAccountingMsg(),accountingBatch.getCode()), IException.CONFIGURATION_ERROR);
		}
		
		this.cfonbService.testBankDetailsField(accountingBatch.getBankDetails());
		
	}
	
}
