package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.expense.ExpenseLineCreateServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseProofFileService;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
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
      Project project,
      Employee employee,
      LocalDate expenseDate,
      String comments,
      Currency currency,
      Boolean toInvoice)
      throws AxelorException {
    ExpenseLine expenseLine =
        super.createBasicExpenseLine(project, employee, expenseDate, comments, currency, toInvoice);

    if (appBusinessProjectService.isApp("business-project")) {
      expenseLine.setToInvoice(getToInvoice(project, toInvoice));
    }
    return expenseLine;
  }

  protected Boolean getToInvoice(Project project, Boolean toInvoice) throws AxelorException {
    if (toInvoice == null && project == null) {
      return false;
    }
    if (toInvoice != null && project == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_NO_PROJECT));
    }
    return toInvoice == null ? project.getIsInvoicingExpenses() : toInvoice;
  }
}
