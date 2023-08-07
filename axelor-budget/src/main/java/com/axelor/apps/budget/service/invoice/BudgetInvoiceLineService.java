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
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;

public interface BudgetInvoiceLineService {

  /**
   * Clear budget distribution, compute the budget key related to this configuration of account and
   * analytic, find the budget related to this key and the invoice date or created on. Then create
   * an automatic budget distribution with the company ex tax total and save the invoice line.
   * Return an error message if a budget distribution is not generated
   *
   * @param invoice
   * @param invoiceLine
   * @return String
   */
  public String computeBudgetDistribution(Invoice invoice, InvoiceLine invoiceLine);

  /**
   * Take all budget distribution and throw an error if the total amount of budget distribution is
   * superior to the company ex tax total of the invoice line
   *
   * @param invoiceLine
   * @throws AxelorException
   */
  public void checkAmountForInvoiceLine(InvoiceLine invoiceLine) throws AxelorException;

  public void computeBudgetDistributionSumAmount(InvoiceLine invoiceLine, Invoice invoice);
}
