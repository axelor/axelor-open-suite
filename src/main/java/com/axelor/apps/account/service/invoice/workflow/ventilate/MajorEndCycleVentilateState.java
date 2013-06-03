package com.axelor.apps.account.service.invoice.workflow.ventilate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.account.service.ReimbursementExportService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;

public class MajorEndCycleVentilateState extends VentilateState {
	
	private PaymentScheduleService paymentScheduleService;
	private ReimbursementExportService reimbursementExportService;
	
	public MajorEndCycleVentilateState(SequenceService SequenceService, MoveService moveService,
			PaymentScheduleService paymentScheduleService, 
			ReimbursementExportService reimbursementExportService, Invoice invoice) {
		
		super(SequenceService, moveService, invoice);
		this.reimbursementExportService = reimbursementExportService;
		this.paymentScheduleService = paymentScheduleService;
		
	}
	
	@Override
	protected void setMove( ) throws AxelorException {
		
		if( invoice.getPaymentSchedule() != null && invoice.getEndOfCycleOk())  {
			
			
			paymentScheduleService.closePaymentSchedule(invoice.getPaymentSchedule());
			
			
		}
		
		super.setMove( );
		
		Move move = invoice.getMove();
		if (move != null && invoice.getPaymentSchedule() != null && invoice.getEndOfCycleOk()) {
			
			reimbursementExportService.createReimbursementInvoice(invoice);
			
		}
		
	}

}