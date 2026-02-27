package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.service.approvalitem.ApprovalItemManagementService;
import com.axelor.apps.businessproject.service.statuschange.ProjectStatusChangeService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.ExpenseHRRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.expense.ExpenseFetchPeriodService;
import com.axelor.apps.project.db.Project;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.Objects;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpenseBusinessProjectRepository extends ExpenseHRRepository {

  private static final Logger log = LoggerFactory.getLogger(ExpenseBusinessProjectRepository.class);
  protected ApprovalItemManagementService approvalItemManagementService;

  @Inject
  public ExpenseBusinessProjectRepository(
      ExpenseFetchPeriodService expenseFetchPeriodService,
      ApprovalItemManagementService approvalItemManagementService) {
    super(expenseFetchPeriodService);
    this.approvalItemManagementService = approvalItemManagementService;
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

    // If an Expense's status is not the CONFIRMED status it should not have an Approval Item
    // As it is either not ready for validation or no longer needs validation
    if (!Objects.equals(expense.getStatusSelect(), ExpenseRepository.STATUS_CONFIRMED)) {
      if (approvalItemManagementService.hasApprovalItem(expense)) {
        approvalItemManagementService.deleteApprovalItem(expense);
      }
    } else {
      Project project = expense.getProject();
      Employee employee = expense.getEmployee(); // can be null
      String currencyName = (expense.getCurrency() != null) ? expense.getCurrency().getName() : "";
      approvalItemManagementService.createApprovalItem(
          expense,
          project,
          employee,
          ApprovalItemRepository.EXPENSE_APPROVAL_ITEM,
          "",
          currencyName,
          expense.getSentDateTime(),
          expense.getInTaxTotal());
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

    approvalItemManagementService.deleteApprovalItem(expense);
  }
}
