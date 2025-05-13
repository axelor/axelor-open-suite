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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.contract.service.ContractVersionService;
import com.axelor.apps.contract.service.WorkflowVentilationContractServiceImpl;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderInvoiceService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Objects;

public class WorkflowVentilationProjectServiceImpl extends WorkflowVentilationContractServiceImpl {

  protected InvoicingProjectRepository invoicingProjectRepo;

  protected TimesheetLineRepository timesheetLineRepo;

  @Inject
  public WorkflowVentilationProjectServiceImpl(
      AccountConfigService accountConfigService,
      InvoicePaymentRepository invoicePaymentRepo,
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoiceService invoiceService,
      SaleOrderInvoiceService saleOrderInvoiceService,
      PurchaseOrderInvoiceService purchaseOrderInvoiceService,
      SaleOrderRepository saleOrderRepository,
      PurchaseOrderRepository purchaseOrderRepository,
      AccountingSituationSupplychainService accountingSituationSupplychainService,
      AppSupplychainService appSupplychainService,
      InvoicingProjectRepository invoicingProjectRepo,
      TimesheetLineRepository timesheetLineRepo,
      StockMoveInvoiceService stockMoveInvoiceService,
      UnitConversionService unitConversionService,
      AppBaseService appBaseService,
      SupplyChainConfigService supplyChainConfigService,
      StockMoveLineRepository stockMoveLineRepository,
      AppAccountService appAccountService,
      InvoiceFinancialDiscountService invoiceFinancialDiscountService,
      InvoiceTermService invoiceTermService,
      ContractVersionService contractVersionService) {
    super(
        accountConfigService,
        invoicePaymentRepo,
        invoicePaymentCreateService,
        invoiceService,
        saleOrderInvoiceService,
        purchaseOrderInvoiceService,
        saleOrderRepository,
        purchaseOrderRepository,
        accountingSituationSupplychainService,
        appSupplychainService,
        stockMoveInvoiceService,
        unitConversionService,
        appBaseService,
        supplyChainConfigService,
        stockMoveLineRepository,
        appAccountService,
        invoiceFinancialDiscountService,
        invoiceTermService,
        contractVersionService);
    this.invoicingProjectRepo = invoicingProjectRepo;
    this.timesheetLineRepo = timesheetLineRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void afterVentilation(Invoice invoice) throws AxelorException {
    super.afterVentilation(invoice);

    if (!Beans.get(AppBusinessProjectService.class).isApp("business-project")) {
      return;
    }

    InvoicingProject invoicingProject =
        invoicingProjectRepo.all().filter("self.invoice.id = ?", invoice.getId()).fetchOne();

    if (invoicingProject != null) {
      for (SaleOrderLine saleOrderLine : invoicingProject.getSaleOrderLineSet()) {
        saleOrderLine.setInvoiced(true);
      }
      for (PurchaseOrderLine purchaseOrderLine : invoicingProject.getPurchaseOrderLineSet()) {
        purchaseOrderLine.setInvoiced(true);
      }
      for (TimesheetLine timesheetLine : invoicingProject.getLogTimesSet()) {
        timesheetLine.setInvoiced(true);

        if (timesheetLine.getProjectTask() == null) {
          continue;
        }

        timesheetLine
            .getProjectTask()
            .setInvoiced(this.checkInvoicedTimesheetLines(timesheetLine.getProjectTask()));
      }
      for (ExpenseLine expenseLine : invoicingProject.getExpenseLineSet()) {
        expenseLine.setInvoiced(true);
      }
      for (ProjectTask projectTask : invoicingProject.getProjectTaskSet()) {
        updateInvoicingStatus(projectTask);
      }

      invoicingProject.setStatusSelect(InvoicingProjectRepository.STATUS_VENTILATED);
      invoicingProjectRepo.save(invoicingProject);
    }
  }

  protected void updateInvoicingStatus(ProjectTask projectTask) {
    BigDecimal newProgress =
        projectTask.getInvoiceLineSet().stream()
            .map(InvoiceLine::getNewProgress)
            .filter(Objects::nonNull)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
    if (ProjectTaskRepository.INVOICING_TYPE_ON_PROGRESS.equals(projectTask.getInvoicingType())) {
      projectTask.setInvoicingProgress(newProgress);
      if (newProgress.compareTo(BigDecimal.valueOf(100)) == 0) {
        projectTask.setInvoiced(true);
      }
    } else {
      projectTask.setInvoiced(true);
    }
  }

  protected boolean checkInvoicedTimesheetLines(ProjectTask projectTask) {

    long timesheetLineCnt =
        timesheetLineRepo
            .all()
            .filter("self.projectTask.id = ?1 AND self.invoiced = ?2", projectTask.getId(), false)
            .count();

    return timesheetLineCnt == 0;
  }
}
