package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.Move
import com.axelor.apps.account.db.MoveLineReport
import com.axelor.apps.account.service.MoveLineReportService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse

@Slf4j
public class MoveLineReportControllerSimple {
	
	
	def void showMoveExported(ActionRequest request, ActionResponse response) {
		
		MoveLineReport moveLineReport = request.context as MoveLineReport
		
		response.view = [
			title : "Ecritures exportées",
			resource : Move.class.name,
			domain : "self.moveLineReport.id = ${moveLineReport.id}"
		]
		
	}
}