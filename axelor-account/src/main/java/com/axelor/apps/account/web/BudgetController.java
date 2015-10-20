package com.axelor.apps.account.web;

import java.util.List;

import com.axelor.apps.account.db.Budget;
import com.axelor.apps.account.db.BudgetLine;
import com.axelor.apps.account.db.repo.BudgetRepository;
import com.axelor.apps.account.service.BudgetService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class BudgetController {
	
	@Inject
	protected BudgetService budgetService;
	
	public void compute(ActionRequest request, ActionResponse response){
		Budget budget = request.getContext().asType(Budget.class);
		response.setValue("totalAmountExpected", budgetService.compute(budget));
	}
	
	public void updateLines(ActionRequest request, ActionResponse response){
		Budget budget = request.getContext().asType(Budget.class);
		budget = Beans.get(BudgetRepository.class).find(budget.getId());
		List<BudgetLine> budgetLineList = budgetService.updateLines(budget);
		response.setValue("budgetLineList", budgetLineList);
	}
	
	public void generatePeriods(ActionRequest request, ActionResponse response) {
		Budget budget = request.getContext().asType(Budget.class);
		response.setValue("budgetLineList", budgetService.generatePeriods(budget));
	}
}
