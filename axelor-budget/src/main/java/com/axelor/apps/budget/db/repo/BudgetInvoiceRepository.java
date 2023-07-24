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
package com.axelor.apps.budget.db.repo;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceLineService;
import com.axelor.apps.businessproject.db.repo.InvoiceProjectRepository;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class BudgetInvoiceRepository extends InvoiceProjectRepository {

  @Override
  public Invoice copy(Invoice entity, boolean deep) {
    Invoice copy = super.copy(entity, deep);

    if (deep) {
      if (copy.getInvoiceLineList() != null && !copy.getInvoiceLineList().isEmpty()) {
        for (InvoiceLine invoiceLine : copy.getInvoiceLineList()) {
          invoiceLine.setBudget(null);
          invoiceLine.setBudgetDistributionSumAmount(BigDecimal.ZERO);
          invoiceLine.clearBudgetDistributionList();
        }
      }
      copy.setBudgetDistributionGenerated(false);
    }

    return copy;
  }

  @Override
  public Invoice save(Invoice invoice) {
    try {
      if (!CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
        BudgetInvoiceLineService budgetInvoiceLineService =
            Beans.get(BudgetInvoiceLineService.class);
        for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
          budgetInvoiceLineService.checkAmountForInvoiceLine(invoiceLine);
        }
      }
    } catch (AxelorException e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }

    super.save(invoice);
    return invoice;
  }
}
