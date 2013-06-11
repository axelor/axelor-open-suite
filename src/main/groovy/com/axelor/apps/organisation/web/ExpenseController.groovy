package com.axelor.apps.organisation.web

import groovy.util.logging.Slf4j
import org.joda.time.LocalDate
import org.joda.time.DateTime
import com.axelor.apps.organisation.db.Expense
import com.axelor.apps.organisation.db.ExpenseLine
import com.axelor.apps.base.service.user.UserInfoService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.persist.Transactional

@Slf4j
class ExpenseController {
	
	def checkValidationStatus(ActionRequest request, ActionResponse response) {
	
		Expense expense = request.context as Expense
		List<ExpenseLine> list = expense.getExpenseLineList()
		if ((list?.isEmpty()) || (list?.any{it?.fileReceived == "2"}) ) {
			response.values = [ "validationStatusSelect" : 2 ]
		}
		else {
			response.values = [ "validationStatusSelect" : 1 ]
		}
	}
	
}
