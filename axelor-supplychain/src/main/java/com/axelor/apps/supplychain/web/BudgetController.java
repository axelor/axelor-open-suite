package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.Budget;
import com.axelor.apps.account.db.repo.BudgetRepository;
import com.axelor.apps.supplychain.service.BudgetSupplychainService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class BudgetController {
    @Inject protected BudgetSupplychainService budgetSupplychainService;
    @Inject protected BudgetRepository budgetRepository;

    public void computeTotalAmountCommited(ActionRequest request, ActionResponse response){
        Budget budget = request.getContext().asType(Budget.class);
        budget = budgetRepository.find(budget.getId());
        response.setValue("totalAmountCommitted", budgetSupplychainService.computeTotalAmountCommitted(budget));
    }
}
