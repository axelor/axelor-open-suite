package com.axelor.apps.account.service.invoice.generator.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.exception.AxelorException;

public interface InvoiceStrategy {

	Invoice generate() throws AxelorException;
	
}
