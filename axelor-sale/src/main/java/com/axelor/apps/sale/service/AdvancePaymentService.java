package com.axelor.apps.sale.service;

import com.axelor.apps.sale.db.AdvancePayment;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface AdvancePaymentService  {
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancelAdvancePayment(AdvancePayment advancePayment);
		
	
}
