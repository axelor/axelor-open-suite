package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.service.statuschange.ProjectStatusChangeService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.ExpenseHRRepository;
import com.axelor.apps.hr.service.expense.ExpenseFetchPeriodService;
import com.axelor.apps.project.db.Project;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class ExpenseBusinessProjectRepository extends ExpenseHRRepository {

  @Inject
  public ExpenseBusinessProjectRepository(ExpenseFetchPeriodService expenseFetchPeriodService) {
    super(expenseFetchPeriodService);
  }

  @Override
  public Expense save(Expense expense) {
    Expense savedExpense = super.save(expense);

    // When an expense gets created, validated or canceled, the project status should be
    // updated to reflect that
    try {
      Beans.get(ProjectStatusChangeService.class).updateProjectStatus(savedExpense.getProject());
    } catch (AxelorAlertException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
    return savedExpense;
  }

  @Override
  public void remove(Expense expense) {
    Project project = expense.getProject();
    super.remove(expense);

    // Update the project's status when an expense gets deleted
    try {
      Beans.get(ProjectStatusChangeService.class).updateProjectStatus(project);
    } catch (AxelorAlertException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
