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
package com.axelor.apps.organisation.web;

import java.math.BigDecimal;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.organisation.db.Expense;
import com.axelor.apps.organisation.db.ExpenseLine;
import com.axelor.apps.organisation.service.ExpenseLineService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ExpenseLineController {
	
	@Inject
	private ExpenseLineService expenseLineService;

	public void compute(ActionRequest request, ActionResponse response) {
	
		ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);
		
		BigDecimal inTaxTotal = BigDecimal.ZERO;
		BigDecimal companyTotal = BigDecimal.ZERO;
		
		try{
			if (expenseLine.getPrice() != null && expenseLine.getQty() != null){
				
				inTaxTotal = ExpenseLineService.computeAmount(expenseLine.getQty(), expenseLine.getPrice());
			}
			
			if(inTaxTotal != null) {

				Expense expense = expenseLine.getExpense();

				if(expense == null) {
					expense = request.getContext().getParentContext().asType(Expense.class);
				}

				if(expense != null) {
					companyTotal = expenseLineService.getCompanyTotal(inTaxTotal, expense);
				}
			}
			
			response.setValue("inTaxTotal", inTaxTotal);
			response.setValue("companyTotal", companyTotal);
		}
		catch(Exception e)  {
			response.setFlash(e.getMessage());
		}	
	}
	
	
	public void getProductInformation(ActionRequest request, ActionResponse response) {
		
		ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);

		Expense expense = expenseLine.getExpense();
		if(expense == null)  {
			expense = request.getContext().getParentContext().asType(Expense.class);
		}

		if(expense != null && expenseLine.getProduct() != null) {

			try  {
				TaxLine tline = expenseLineService.getTaxLine(expense,expenseLine);
				response.setValue("taxLine", tline);
				response.setValue("shortDescription", expenseLine.getProduct().getName());
				response.setValue("unit", expenseLine.getProduct().getUnit());
				
				response.setValue("price", expenseLineService.getUnitPrice(expense, expenseLine));
				
			}
			catch(Exception e)  {
				e.printStackTrace();
				response.setFlash(e.getMessage()); 
				this.resetProductInformation(response);
			}
		}
		else {
			this.resetProductInformation(response);
		}
	}
	
	
	public void resetProductInformation(ActionResponse response)  {
		
		response.setValue("shortDescription", null);
		response.setValue("unit", null);
		response.setValue("price", null);
		
	}
}
