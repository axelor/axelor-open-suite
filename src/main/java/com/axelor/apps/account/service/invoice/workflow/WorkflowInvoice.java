package com.axelor.apps.account.service.invoice.workflow;

import com.axelor.apps.account.db.Invoice;
import com.axelor.exception.AxelorException;

public abstract class WorkflowInvoice {

	protected Invoice invoice;
	
	protected WorkflowInvoice (Invoice invoice){
		this.invoice = invoice;
	}
	
	public abstract void process() throws AxelorException;
	
}
