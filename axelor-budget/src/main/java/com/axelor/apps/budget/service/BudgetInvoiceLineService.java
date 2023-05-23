package com.axelor.apps.budget.service;

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
   * @param invoiceLine
   * @return String
   */
  public String computeBudgetDistribution(InvoiceLine invoiceLine);

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
