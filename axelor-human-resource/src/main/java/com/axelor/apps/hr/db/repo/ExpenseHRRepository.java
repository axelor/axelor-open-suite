package com.axelor.apps.hr.db.repo;

import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.inject.Beans;

public class ExpenseHRRepository extends ExpenseRepository {
    @Override
    public Expense save(Expense expense) {
        expense = super.save(expense);
        Beans.get(ExpenseService.class).setDraftSequence(expense);

        return expense;
    }
}
