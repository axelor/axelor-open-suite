/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.accountorganisation.web;

import com.axelor.apps.accountorganisation.service.ExpenseLineService;
import com.axelor.apps.organisation.db.Expense;
import com.axelor.apps.organisation.db.ExpenseLine;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ExpenseLineController {
	
	@Inject
	private ExpenseLineService expenseLineService;

	
	public void getProductInformation(ActionRequest request, ActionResponse response) {

		ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);

		if(expenseLine != null) {
			Expense expense = expenseLine.getExpense();
			if(expense == null)  {
				expense = request.getContext().getParentContext().asType(Expense.class);
			}

			if(expense != null && expenseLine.getProduct() != null) {

				try  {
					
					response.setValue("taxLine", expenseLineService.getTaxLine(expense, expenseLine));
					
				}
				catch(Exception e)  {
					response.setFlash(e.getMessage()); 
				}
			}
		}
	}
}
