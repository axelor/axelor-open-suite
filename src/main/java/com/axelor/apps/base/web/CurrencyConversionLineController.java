package com.axelor.apps.base.web;

import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class CurrencyConversionLineController {

	public void checkDate(ActionRequest request, ActionResponse response) {
	
		CurrencyConversionLine ccl = request.getContext().asType(CurrencyConversionLine.class);
		
		if (CurrencyConversionLine.all().filter("self.startCurrency = ?1 and self.endCurrency = ?2 and self.toDate = null",ccl.getStartCurrency(),ccl.getEndCurrency()).count() > 0) {
			String msg = "WARNING : Last conversion rate period has not been closed";
			response.setFlash(msg);
			response.setValue("fromDate", "");		
		}
		else if (CurrencyConversionLine.all().filter("self.startCurrency = ?1 and self.endCurrency = ?2 and self.toDate >= ?3",ccl.getStartCurrency(),ccl.getEndCurrency(),ccl.getFromDate()).count() > 0) {
			String msg = "WARNING : Last conversion rate period has not ended";
			response.setFlash(msg);
		}
	}
}
