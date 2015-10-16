package com.axelor.apps.business.project.service;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.AnalyticDistributionLineService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.config.AccountConfigHRService;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class ExpenseProjectService extends ExpenseService  {

	@Inject
	public ExpenseProjectService(MoveService moveService, ExpenseRepository expenseRepository, MoveLineService moveLineService,
			AccountManagementServiceAccountImpl accountManagementService, GeneralService generalService,
			AccountConfigHRService accountConfigService, AnalyticDistributionLineService analyticDistributionLineService) {
		
		super(moveService, expenseRepository, moveLineService, accountManagementService, generalService, accountConfigService, analyticDistributionLineService);
	
	}

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
