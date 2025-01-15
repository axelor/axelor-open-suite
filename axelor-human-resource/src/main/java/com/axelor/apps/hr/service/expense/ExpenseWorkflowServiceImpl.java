package com.axelor.apps.hr.service.expense;

import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.google.inject.persist.Transactional;

public class ExpenseWorkflowServiceImpl implements ExpenseWorkflowService {

  @Transactional
  @Override
  public void backToDraft(Expense expense) {
    expense.setStatusSelect(ExpenseRepository.STATUS_DRAFT);
  }
}
