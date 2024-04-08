package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectStatusRepository;
import com.axelor.apps.project.service.ProjectClosingControlServiceImpl;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.repo.AppBusinessProjectRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Arrays;

public class BusinessProjectClosingControlServiceImpl extends ProjectClosingControlServiceImpl {

  protected AppBusinessProjectService appBusinessProjectService;
  protected SaleOrderRepository saleOrderRepository;
  protected PurchaseOrderRepository purchaseOrderRepository;
  protected ContractRepository contractRepository;
  protected TimesheetLineRepository timesheetLineRepository;
  protected ExpenseLineRepository expenseLineRepository;

  @Inject
  public BusinessProjectClosingControlServiceImpl(
      ProjectRepository projectRepository,
      ProjectStatusRepository projectStatusRepository,
      AppBusinessProjectService appBusinessProjectService,
      SaleOrderRepository saleOrderRepository,
      PurchaseOrderRepository purchaseOrderRepository,
      ContractRepository contractRepository,
      TimesheetLineRepository timesheetLineRepository,
      ExpenseLineRepository expenseLineRepository) {
    super(projectRepository, projectStatusRepository);
    this.appBusinessProjectService = appBusinessProjectService;
    this.saleOrderRepository = saleOrderRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.contractRepository = contractRepository;
    this.timesheetLineRepository = timesheetLineRepository;
    this.expenseLineRepository = expenseLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public String finishProject(Project project) throws AxelorException {
    String errorMessage = super.finishProject(project);
    Integer closingProjectRuleSelect =
        appBusinessProjectService.getAppBusinessProject().getClosingProjectRuleSelect();

    if (closingProjectRuleSelect == AppBusinessProjectRepository.CLOSING_PROJECT_RULE_NONE) {
      return errorMessage;
    }

    if (!areSaleOrdersFinished(project)) {
      errorMessage +=
          "<br/>"
              + I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_SALE_ORDER_IN_PROGRESS);
    }

    if (!arePurchaseOrdersFinished(project)) {
      errorMessage +=
          "<br/>"
              + I18n.get(
                  BusinessProjectExceptionMessage.PROJECT_CLOSING_PURCHASE_ORDER_IN_PROGRESS);
    }

    if (!areContractsFinished(project)) {
      errorMessage +=
          "<br/>" + I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_CONTRACT_IN_PROGRESS);
    }

    if (appBusinessProjectService.isApp("timesheet") && !areTimesheetLinesFinished(project)) {
      errorMessage +=
          "<br/>" + I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_TIMESHEET_IN_PROGRESS);
    }

    if (appBusinessProjectService.isApp("expense") && !areExpenseLinesFinished(project)) {
      errorMessage +=
          "<br/>" + I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_EXPENSE_IN_PROGRESS);
    }
    if (closingProjectRuleSelect == AppBusinessProjectRepository.CLOSING_PROJECT_RULE_BLOCKING) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_BLOCKING_MESSAGE)
              + errorMessage);
    } else {
      return I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_NON_BLOCKING_MESSAGE)
          + errorMessage;
    }
  }

  protected boolean areSaleOrdersFinished(Project project) {
    return saleOrderRepository
            .all()
            .filter(
                "self.project.id = :projectId AND EXISTS (SELECT sol FROM self.saleOrderLineList sol WHERE sol.invoiced = FALSE)")
            .bind("projectId", project.getId())
            .count()
        == 0;
  }

  protected boolean arePurchaseOrdersFinished(Project project) {
    return purchaseOrderRepository
            .all()
            .filter(
                "self.project.id = :projectId AND (self.receiptState != :receiptState OR EXISTS (SELECT pol FROM self.purchaseOrderLineList pol WHERE pol.invoiced = FALSE))")
            .bind("projectId", project.getId())
            .bind("receiptState", PurchaseOrderRepository.STATE_RECEIVED)
            .count()
        == 0;
  }

  protected boolean areContractsFinished(Project project) {
    return contractRepository
            .all()
            .filter("self.project.id = :projectId AND self.statusSelect != :status")
            .bind("projectId", project.getId())
            .bind("status", ContractRepository.CLOSED_CONTRACT)
            .count()
        == 0;
  }

  protected boolean areTimesheetLinesFinished(Project project) {
    return timesheetLineRepository
            .all()
            .filter(
                "self.project.id = :projectId AND self.toInvoice = TRUE AND self.invoiced = FALSE")
            .bind("projectId", project.getId())
            .count()
        == 0;
  }

  protected boolean areExpenseLinesFinished(Project project) {
    return expenseLineRepository
            .all()
            .filter(
                "self.project.id = :projectId AND self.expense.statusSelect NOT IN :finishedStatusList")
            .bind("projectId", project.getId())
            .bind(
                "finishedStatusList",
                Arrays.asList(
                    ExpenseRepository.STATUS_REIMBURSED, ExpenseRepository.STATUS_CANCELED))
            .count()
        == 0;
  }
}
