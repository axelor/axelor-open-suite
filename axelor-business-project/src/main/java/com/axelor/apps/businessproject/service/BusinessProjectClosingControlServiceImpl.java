/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectStatusRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
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
  protected SaleOrderLineRepository saleOrderLineRepository;
  protected PurchaseOrderLineRepository purchaseOrderLineRepository;
  protected ContractRepository contractRepository;
  protected TimesheetLineRepository timesheetLineRepository;
  protected ExpenseLineRepository expenseLineRepository;

  @Inject
  public BusinessProjectClosingControlServiceImpl(
      AppProjectService appProjectService,
      AppBusinessProjectService appBusinessProjectService,
      ProjectRepository projectRepository,
      ProjectStatusRepository projectStatusRepository,
      SaleOrderLineRepository saleOrderLineRepository,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      ContractRepository contractRepository,
      TimesheetLineRepository timesheetLineRepository,
      ExpenseLineRepository expenseLineRepository) {
    this.appProjectService = appProjectService;
    this.appBusinessProjectService = appBusinessProjectService;
    this.projectRepository = projectRepository;
    this.projectStatusRepository = projectStatusRepository;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
    this.contractRepository = contractRepository;
    this.timesheetLineRepository = timesheetLineRepository;
    this.expenseLineRepository = expenseLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public String checkProjectState(Project project) throws AxelorException {
    StringBuilder errorMessage = new StringBuilder();
    Integer closingProjectRuleSelect =
        appBusinessProjectService.getAppBusinessProject().getClosingProjectRuleSelect();
    if (!project.getIsBusinessProject()
        || closingProjectRuleSelect == AppBusinessProjectRepository.CLOSING_PROJECT_RULE_NONE) {
      return errorMessage.toString();
    }

    if (!areSaleOrderLinesFinished(project)) {
      errorMessage
          .append("<br/>")
          .append(
              I18n.get(
                  BusinessProjectExceptionMessage.PROJECT_CLOSING_SALE_ORDER_LINE_NOT_INVOICED));
    }

    if (!arePurchaseOrderLinesInvoiced(project)) {
      errorMessage
          .append("<br/>")
          .append(
              I18n.get(
                  BusinessProjectExceptionMessage
                      .PROJECT_CLOSING_PURCHASE_ORDER_LINE_NOT_INVOICED));
    }
    if (!arePurchaseOrderLinesReceived(project)) {
      errorMessage
          .append("<br/>")
          .append(
              I18n.get(
                  BusinessProjectExceptionMessage
                      .PROJECT_CLOSING_PURCHASE_ORDER_LINE_NOT_RECEIVED));
    }

    if (!areContractsFinished(project)) {
      errorMessage
          .append("<br/>")
          .append(I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_CONTRACT_IN_PROGRESS));
    }

    if (appBusinessProjectService.isApp("timesheet") && !areTimesheetLinesFinished(project)) {
      errorMessage
          .append("<br/>")
          .append(
              I18n.get(
                  BusinessProjectExceptionMessage.PROJECT_CLOSING_TIMESHEET_LINE_NOT_INVOICED));
    }

    if (appBusinessProjectService.isApp("expense") && !areExpenseLinesFinished(project)) {
      errorMessage
          .append("<br/>")
          .append(
              I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_EXPENSE_LINE_NOT_INVOICED));
    }

    if (errorMessage.length() == 0) {
      return errorMessage.toString();
    }

    if (closingProjectRuleSelect == AppBusinessProjectRepository.CLOSING_PROJECT_RULE_BLOCKING) {
      return errorMessage
          .insert(0, I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_BLOCKING_MESSAGE))
          .toString();
    } else if (closingProjectRuleSelect
        == AppBusinessProjectRepository.CLOSING_PROJECT_RULE_NON_BLOCKING) {
      return errorMessage
          .insert(0, I18n.get(BusinessProjectExceptionMessage.PROJECT_CLOSING_NON_BLOCKING_MESSAGE))
          .toString();
    } else {
      return errorMessage.toString();
    }
  }

  protected boolean areSaleOrderLinesFinished(Project project) {
    return saleOrderLineRepository
            .all()
            .filter("self.project.id = :projectId AND self.amountInvoiced != self.exTaxTotal")
            .bind("projectId", project.getId())
            .count()
        == 0;
  }

  protected boolean arePurchaseOrderLinesInvoiced(Project project) {
    return purchaseOrderLineRepository
            .all()
            .filter("self.project.id = :projectId AND self.amountInvoiced != self.exTaxTotal")
            .bind("projectId", project.getId())
            .count()
        == 0;
  }

  protected boolean arePurchaseOrderLinesReceived(Project project) {
    return purchaseOrderLineRepository
            .all()
            .filter("self.project.id = :projectId AND self.receiptState != :receiptState")
            .bind("projectId", project.getId())
            .bind("receiptState", PurchaseOrderLineRepository.RECEIPT_STATE_RECEIVED)
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
