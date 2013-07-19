package com.axelor.apps.accountorganisation.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.db.Task;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectInvoiceService {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectInvoiceService.class);

	@Inject
	private TaskInvoiceService taskInvoiceService;


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateInvoice(Project project) throws AxelorException {

		// Check if the fields clientPartner, contactPartner and company are not empty
		if(project.getClientPartner() != null && project.getContactPartner() != null && project.getCompany() != null) {

			Invoice invoice = this.createInvoice(project);
			invoice.save();
			return invoice;
		}
		return null;
	}

	public Invoice createInvoice(Project project) throws AxelorException {

		
		InvoiceGenerator invoiceGenerator = new InvoiceGenerator(IInvoice.CLIENT_SALE, project.getCompany(), project.getClientPartner(), project.getContactPartner()) {

			@Override
			public Invoice generate() throws AxelorException {

				return super.createInvoiceHeader();
			}
		};

		Invoice invoice = invoiceGenerator.generate();
		invoiceGenerator.populate(invoice, this.createInvoiceLines(invoice, project));
		invoiceGenerator.computeInvoice(invoice);
		return invoice;
	}
	
	
	@Transactional
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, Project project) throws AxelorException  {
		
		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		
		for(Task task : project.getTaskList()) {
			
			if(task.getIsToInvoice() && task.getSalesOrderLine() != null) {
				
				invoiceLineList.addAll(taskInvoiceService.createInvoiceLine(invoice, task));
			}
		}
		return invoiceLineList;	
	}
	
}
