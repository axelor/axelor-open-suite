package com.axelor.apps.organisation.web

import groovy.util.logging.Slf4j
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject
import com.axelor.apps.organisation.db.OvertimeLine

@Slf4j
class OvertimeLineController {
	def computeTotal(ActionRequest request, ActionResponse response) {
		
		OvertimeLine overtimeLine = request.context as OvertimeLine
		
		if (overtimeLine.quantity && overtimeLine.unitPrice) {
			response.values = [ "total" : overtimeLine.quantity*overtimeLine.unitPrice]
		}
		else {
			response.values = [ "total" : 0.00]
		}
	}
}
