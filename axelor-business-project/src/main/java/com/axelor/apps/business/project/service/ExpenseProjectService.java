package com.axelor.apps.business.project.service;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.exception.AxelorException;

public class ExpenseProjectService extends ExpenseService  {

	@Override
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<ExpenseLine> expenseLineList, int priority) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		int count = 0;
		for(ExpenseLine expenseLine : expenseLineList)  {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, expenseLine,priority*100+count));
			count++;
			expenseLine.setInvoiced(true);
			invoiceLineList.get(invoiceLineList.size()-1).setProject(expenseLine.getProjectTask());

		}

		return invoiceLineList;

	}
}
