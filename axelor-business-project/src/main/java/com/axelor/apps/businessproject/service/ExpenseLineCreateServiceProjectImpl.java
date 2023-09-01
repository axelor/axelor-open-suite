package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.expense.ExpenseLineCreateServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseProofFileService;
import com.axelor.apps.project.db.Project;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public class ExpenseLineCreateServiceProjectImpl extends ExpenseLineCreateServiceImpl {
  protected AppBusinessProjectService appBusinessProjectService;

  @Inject
  public ExpenseLineCreateServiceProjectImpl(
      ExpenseLineRepository expenseLineRepository,
      AppHumanResourceService appHumanResourceService,
      KilometricService kilometricService,
      HRConfigService hrConfigService,
      AppBaseService appBaseService,
      ExpenseProofFileService expenseProofFileService,
      AppBusinessProjectService appBusinessProjectService) {
    super(
        expenseLineRepository,
        appHumanResourceService,
        kilometricService,
        hrConfigService,
        appBaseService,
        expenseProofFileService);
    this.appBusinessProjectService = appBusinessProjectService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  protected ExpenseLine createBasicExpenseLine(
      Project project, Employee employee, LocalDate expenseDate, String comments)
      throws AxelorException {
    ExpenseLine expenseLine =
        super.createBasicExpenseLine(project, employee, expenseDate, comments);

    if (appBusinessProjectService.isApp("business-project")) {
      if (project != null) {
        expenseLine.setToInvoice(project.getIsInvoicingExpenses());
      }
    }
    return expenseLine;
  }
}
