package com.axelor.apps.account.service.invoice.workflow.cancel;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.workflow.WorkflowInvoice;
import com.axelor.apps.base.db.Status;
import com.axelor.exception.AxelorException;

public class CancelState extends WorkflowInvoice {
	
	public CancelState(Invoice invoice){ super(invoice); }
	
	@Override
	public void process() throws AxelorException {
		
		setStatus(invoice);
		
	}
	
	protected void setStatus( Invoice invoice ){
		invoice.setStatus(Status.all().filter("self.code = 'can'").fetchOne());
	}
	
}