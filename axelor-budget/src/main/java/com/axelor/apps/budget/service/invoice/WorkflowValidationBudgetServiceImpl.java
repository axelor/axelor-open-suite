/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.service.WorkflowValidationServiceProjectImpl;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class WorkflowValidationBudgetServiceImpl extends WorkflowValidationServiceProjectImpl {

  protected AppBudgetService appBudgetService;
  protected BudgetInvoiceService budgetInvoiceService;

  @Inject
  public WorkflowValidationBudgetServiceImpl(
      InvoicingProjectRepository invoicingProjectRepo,
      AppBudgetService appBudgetService,
      BudgetInvoiceService budgetInvoiceService) {
    super(invoicingProjectRepo);
    this.appBudgetService = appBudgetService;
    this.budgetInvoiceService = budgetInvoiceService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void afterValidation(Invoice invoice) throws AxelorException {
    super.afterValidation(invoice);

    if ((invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
        || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND)) {
      if (appBudgetService.getAppBudget() != null
          && appBudgetService.getAppBudget().getManageMultiBudget()) {
        budgetInvoiceService.generateBudgetDistribution(invoice);
      }
    }
  }
}
