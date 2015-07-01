package com.axelor.apps.business.project.service;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImp;
import com.axelor.exception.AxelorException;

public class TimesheetProjectServiceImp extends TimesheetServiceImp{

	@Override
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<TimesheetLine> timesheetLineList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for(TimesheetLine timesheetLine : timesheetLineList)  {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, timesheetLine));
			timesheetLine.setInvoiced(true);
			invoiceLineList.get(invoiceLineList.size()-1).setProject(timesheetLine.getProjectTask());
		}

		return invoiceLineList;

	}
}
