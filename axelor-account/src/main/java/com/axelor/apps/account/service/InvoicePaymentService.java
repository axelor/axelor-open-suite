package com.axelor.apps.account.service;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface InvoicePaymentService   {
	

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move createMoveForInvoicePayment(InvoicePayment invoicePayment) throws AxelorException;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancel(InvoicePayment invoicePayment) throws AxelorException; 
	
	
	
}
