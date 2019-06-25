package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.Budget;
import com.axelor.apps.account.db.repo.BudgetRepository;
import com.axelor.apps.supplychain.service.BudgetSupplychainService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class BudgetController {

    public void computeTotalAmountCommited(ActionRequest request, ActionResponse response){
        try {
            Budget budget = request.getContext().asType(Budget.class);
            budget = Beans.get(BudgetRepository.class).find(budget.getId());
            response.setValue("totalAmountCommitted", Beans.get(BudgetSupplychainService.class).computeTotalAmountCommitted(budget));
        } catch (Exception e){
            TraceBackService.trace(response, e);
        }

    }
}
