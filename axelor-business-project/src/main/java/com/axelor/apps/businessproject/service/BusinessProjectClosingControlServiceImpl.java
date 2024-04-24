package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectStatusRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.repo.AppBusinessProjectRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BusinessProjectClosingControlServiceImpl
    implements BusinessProjectClosingControlService {

  protected AppProjectService appProjectService;
  protected AppBusinessProjectService appBusinessProjectService;
  protected ProjectRepository projectRepository;
  protected ProjectStatusRepository projectStatusRepository;
  protected SaleOrderRepository saleOrderRepository;
  protected PurchaseOrderRepository purchaseOrderRepository;
  protected ContractRepository contractRepository;
  protected TimesheetLineRepository timesheetLineRepository;
  protected ExpenseLineRepository expenseLineRepository;

  @Inject
  public BusinessProjectClosingControlServiceImpl(
      AppProjectService appProjectService,
      AppBusinessProjectService appBusinessProjectService,
      ProjectRepository projectRepository,
      ProjectStatusRepository projectStatusRepository,
      SaleOrderRepository saleOrderRepository,
      PurchaseOrderRepository purchaseOrderRepository,
      ContractRepository contractRepository,
      TimesheetLineRepository timesheetLineRepository,
      ExpenseLineRepository expenseLineRepository) {
    this.appProjectService = appProjectService;
    this.appBusinessProjectService = appBusinessProjectService;
    this.projectRepository = projectRepository;
    this.projectStatusRepository = projectStatusRepository;
    this.saleOrderRepository = saleOrderRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.contractRepository = contractRepository;
    this.timesheetLineRepository = timesheetLineRepository;
    this.expenseLineRepository = expenseLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public String checkProjectState(Project project) throws AxelorException {
    String errorMessage = "";
    if (!project.getIsBusinessProject()) {
      return errorMessage;
    }

    Integer closingProjectRuleSelect =
        appBusinessProjectService.getAppBusinessProject().getClosingProjectRuleSelect();

    if (closingProjectRuleSelect == AppBusinessProjectRepository.CLOSING_PROJECT_RULE_NONE) {
      return errorMessage;
    }

    if (!areSaleOrdersFinished(project)) {
      errorMessage +=
          "<br/>"
              + I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_SALE_ORDER_NOT_INVOICED);
    }

    if (!arePurchaseOrdersInvoiced(project)) {
      errorMessage +=
          "<br/>"
              + I18n.get(
                  BusinessProjectExceptionMessage.PROJECT_CLOSING_PURCHASE_ORDER_NOT_INVOICED);
    }
    if (!arePurchaseOrdersReceived(project)) {
      errorMessage +=
          "<br/>"
              + I18n.get(
                  BusinessProjectExceptionMessage.PROJECT_CLOSING_PURCHASE_ORDER_NOT_RECEIVED);
    }

    if (!areContractsFinished(project)) {
      errorMessage +=
          "<br/>" + I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_CONTRACT_IN_PROGRESS);
    }

    if (appBusinessProjectService.isApp("timesheet") && !areTimesheetLinesFinished(project)) {
      errorMessage +=
          "<br/>"
              + I18n.get(
                  BusinessProjectExceptionMessage.PROJECT_CLOSING_TIMESHEET_LINE_NOT_INVOICED);
    }

    if (appBusinessProjectService.isApp("expense") && !areExpenseLinesFinished(project)) {
      errorMessage +=
          "<br/>"
              + I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_EXPENSE_LINE_NOT_INVOICED);
    }
    if (closingProjectRuleSelect == AppBusinessProjectRepository.CLOSING_PROJECT_RULE_BLOCKING) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_BLOCKING_MESSAGE)
              + errorMessage);
    } else if (closingProjectRuleSelect
            == AppBusinessProjectRepository.CLOSING_PROJECT_RULE_NON_BLOCKING
        && !errorMessage.isEmpty()) {
      return I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_NON_BLOCKING_MESSAGE)
          + errorMessage;
    } else {
      return errorMessage;
    }
  }

  protected boolean areSaleOrdersFinished(Project project) {
    return saleOrderRepository
            .all()
            .filter("self.project.id = :projectId AND self.amountInvoiced != self.exTaxTotal")
            .bind("projectId", project.getId())
            .count()
        == 0;
  }

  protected boolean arePurchaseOrdersInvoiced(Project project) {
    return purchaseOrderRepository
            .all()
            .filter("self.project.id = :projectId AND self.amountInvoiced != self.exTaxTotal")
            .bind("projectId", project.getId())
            .count()
        == 0;
  }

  protected boolean arePurchaseOrdersReceived(Project project) {
    return purchaseOrderRepository
            .all()
            .filter("self.project.id = :projectId AND self.receiptState != :receiptState")
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
                "self.project.id = :projectId AND self.toInvoice = TRUE AND self.invoiced = FALSE")
            .bind("projectId", project.getId())
            .count()
        == 0;
  }
}
