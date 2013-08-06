package com.axelor.apps.organisation.web;

import java.util.List;

import com.axelor.apps.organisation.db.Expense;
import com.axelor.apps.organisation.db.ExpenseLine;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ExpenseController {

	public void checkValidationStatus(ActionRequest request, ActionResponse response) {
	
		Expense expense = request.getContext().asType(Expense.class);
		List<ExpenseLine> list = expense.getExpenseLineList();
		boolean checkFileReceived = false;
		
		if(list != null && !list.isEmpty()) {
			for(ExpenseLine expenseLine : list) {
				if(expenseLine.getFileReceived() == 2) {
					checkFileReceived = true;
					break;
				}
			}
		}
		if ((list != null && list.isEmpty()) || checkFileReceived) {
			response.setValue("validationStatusSelect", 2);
		}
		else {
			response.setValue("validationStatusSelect", 1);
		}
	}
}
