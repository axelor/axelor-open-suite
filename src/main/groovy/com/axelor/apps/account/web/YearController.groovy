package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.Year
import com.axelor.apps.account.service.YearService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

@Slf4j
class YearController {
	
	@Inject
	private YearService ys
	
	def void close(ActionRequest request, ActionResponse response) {
		
		Year year = request.context as Year
		
		try  {
			
			ys.closeYear(year)
			response.reload = true
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
			
	}
}
