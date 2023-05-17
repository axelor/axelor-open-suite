package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Invoice;

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
}
