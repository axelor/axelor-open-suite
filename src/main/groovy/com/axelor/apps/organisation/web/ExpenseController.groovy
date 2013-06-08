package com.axelor.apps.organisation.web

import groovy.util.logging.Slf4j
import org.joda.time.LocalDate
import com.axelor.apps.organisation.db.Expense
import com.axelor.apps.organisation.db.ExpenseLine
import com.axelor.apps.base.service.user.UserInfoService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse

@Slf4j
class ExpenseController {
	
	def checkValidationStatus(ActionRequest request, ActionResponse response) {
	
		Expense expense = request.context as Expense
		List <ExpenseLine> list = expense.expenseLineList
		if ((list?.isEmpty()) || (list?.any{it?.fileReceived == 2}) ) {
			response.values = [ "validationStatusSelect" : 2,
				"validationDate" : "",
				"validatedByUserInfo" : ""]	
			response.flash = list?.collect(){it?.fileReceived == 2}.size() +" "+new LocalDate()
		}
		else {
			response.values = [ "validationStatusSelect" : 1,
				"validationDate" : new LocalDate(),
				"validatedByUserInfo" : new UserInfoService().getUserInfo()]
		}
	}
}
