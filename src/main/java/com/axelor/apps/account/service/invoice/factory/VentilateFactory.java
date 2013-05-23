package com.axelor.apps.account.service.invoice.factory;

import javax.inject.Inject;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.account.service.ReimbursementExportService;
import com.axelor.apps.account.service.invoice.workflow.ventilate.MajorEndCycleVentilateState;
import com.axelor.apps.account.service.invoice.workflow.ventilate.VentilateState;
import com.axelor.apps.base.service.administration.SequenceService;

public class VentilateFactory {
	
	@Inject
	private SequenceService SequenceService;
	
	@Inject
	private MoveService moveService;
	
	@Inject
	private PaymentScheduleService paymentScheduleService;
	
	@Inject
	private ReimbursementExportService reimbursementExportService;

	public VentilateState getVentilator(Invoice invoice){
		
		return ventilatorByType(invoice);
		
	}
	
	protected VentilateState ventilatorByType(Invoice invoice){
		
		if(invoice.getEndOfCycleOk())  {
			return new MajorEndCycleVentilateState(SequenceService, moveService, paymentScheduleService, reimbursementExportService, invoice);
		}
		else  {
			return new VentilateState(SequenceService, moveService, invoice);
		}
		
	}
	
}
