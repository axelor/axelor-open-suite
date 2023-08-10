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
import com.axelor.apps.budget.db.BudgetDistribution;
import java.util.List;

public interface BudgetInvoiceService {

  /**
   * For each invoice line : Clear budget distribution, compute the budget key related to this
   * configuration of account and analytic, find the budget related to this key and the invoice date
   * or created on. Then create an automatic budget distribution with the company ex tax total and
   * save the invoice line. If a budget distribution is not generated, save the invoice line name in
   * an alert message that will be return.
   *
   * @param invoice
   * @return String
   */
  public String computeBudgetDistribution(Invoice invoice);

  /**
   * For all budgets related to this invoice, check budget exceed based on global budget control on
   * budget exceed then compute an error message if needed then return it.
   *
   * @param invoice
   * @return String
   */
  public String getBudgetExceedAlert(Invoice invoice);

  /**
   * Return if there is budget distribution on any invoice line
   *
   * @param invoice
   * @return boolean
   */
  public boolean isBudgetInLines(Invoice invoice);

  public void updateBudgetLinesFromInvoice(Invoice invoice);

  public void generateBudgetDistribution(Invoice invoice);

  public void setComputedBudgetLinesAmount(List<InvoiceLine> invoiceLineList);

  /**
   * Select budget line linked to the budget distribution at invoice's date or invoice's created on
   * if date is null and modify totals (realized without po, realized, to be committed, firm gap and
   * available) as a without purchase order line
   *
   * @param budgetDistribution, invoice
   */
  public void updateLineWithNoPO(BudgetDistribution budgetDistribution, Invoice invoice);

  /**
   * Select budget line linked to the budget distribution at purchase order's order date and modify
   * totals (realized with po, realized, to be committed, firm gap and available) as a without
   * purchase order line
   *
   * @param budgetDistribution, invoice
   */
  public void updateLineWithPO(
      BudgetDistribution budgetDistribution, Invoice invoice, InvoiceLine invoiceLine);
}
