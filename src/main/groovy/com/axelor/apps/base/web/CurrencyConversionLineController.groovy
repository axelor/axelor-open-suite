package com.axelor.apps.base.web

import groovy.util.logging.Slf4j
import com.axelor.apps.base.db.CurrencyConversionLine
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse

@Slf4j
class CurrencyConversionLineController {
	
	def checkDate(ActionRequest request, ActionResponse response) {
	
		CurrencyConversionLine ccl = request.context as CurrencyConversionLine
		
		if (CurrencyConversionLine.all().filter("self.startCurrency = ?1 and self.endCurrency = ?2 and self.toDate = null",ccl.startCurrency,ccl.endCurrency)?.count() > 0) {
			def msg = "Last conversion rate period has not been closed"
			response.flash = msg
			response.values = [ "fromDate" : "" ]
		
		}
		else if (CurrencyConversionLine.all().filter("self.startCurrency = ?1 and self.endCurrency = ?2 and self.toDate >= ?3",ccl.startCurrency,ccl.endCurrency,ccl.fromDate)?.count() > 0) {
			def msg = "Last conversion rate period has not ended"
			response.flash = msg
			response.values = [ "fromDate" : "" ]
		}
	}
}
