/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.db.repo;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.InvoiceValidationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceLineService;
import com.axelor.apps.businessproject.db.repo.InvoiceProjectRepository;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class BudgetInvoiceRepository extends InvoiceProjectRepository {

  @Inject
  public BudgetInvoiceRepository(
      AppBusinessProjectService appBusinessProjectService,
      InvoiceValidationService invoiceValidationService) {
    super(appBusinessProjectService, invoiceValidationService);
  }

  @Override
  public Invoice copy(Invoice entity, boolean deep) {
    Invoice copy = super.copy(entity, deep);

    if (deep && Beans.get(AppBudgetService.class).isApp("budget")) {
      if (copy.getInvoiceLineList() != null && !copy.getInvoiceLineList().isEmpty()) {
        BudgetToolsService budgetToolsService = Beans.get(BudgetToolsService.class);
        for (InvoiceLine invoiceLine : copy.getInvoiceLineList()) {
          invoiceLine.setBudget(null);
          invoiceLine.clearBudgetDistributionList();
          invoiceLine.setBudgetRemainingAmountToAllocate(
              budgetToolsService.getBudgetRemainingAmountToAllocate(
                  invoiceLine.getBudgetDistributionList(), invoiceLine.getCompanyExTaxTotal()));
        }
      }
      copy.setBudgetDistributionGenerated(false);
    }

    return copy;
  }

  @Override
  public Invoice save(Invoice invoice) {
    try {
      if (!CollectionUtils.isEmpty(invoice.getInvoiceLineList())
          && Beans.get(AppBudgetService.class).isApp("budget")) {
        BudgetInvoiceLineService budgetInvoiceLineService =
            Beans.get(BudgetInvoiceLineService.class);
        boolean negateAmount =
            invoice.getOperationTypeSelect() == OPERATION_TYPE_SUPPLIER_REFUND
                || invoice.getOperationTypeSelect() == OPERATION_TYPE_CLIENT_REFUND
                || invoice.getOperationSubTypeSelect() == OPERATION_SUB_TYPE_ADVANCE;
        for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
          budgetInvoiceLineService.checkAmountForInvoiceLine(invoiceLine);
          if (negateAmount) {
            budgetInvoiceLineService.negateAmount(invoiceLine, invoice);
          }
        }
      }
    } catch (AxelorException e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }

    super.save(invoice);
    return invoice;
  }
}
