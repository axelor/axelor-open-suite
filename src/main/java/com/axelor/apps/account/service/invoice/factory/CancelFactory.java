package com.axelor.apps.account.service.invoice.factory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.workflow.cancel.CancelState;

public class CancelFactory {
	
	public CancelState getCanceller(Invoice invoice){
		
		return new CancelState( invoice );
		
	}
	
}
