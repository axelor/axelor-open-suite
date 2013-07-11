package com.axelor.apps.accountorganisation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectInvoiceService {

	private static final Logger LOG = LoggerFactory.getLogger(TaskInvoiceService.class);

	@Inject
	private CurrencyService currencyService;

	private LocalDate today;

	public ProjectInvoiceService() {
		this.today = GeneralService.getTodayDate();
	}

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
				
				invoiceLineList.addAll(this.createInvoiceLine(invoice, task));
			}
		}
		return invoiceLineList;	
	}
	
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, BigDecimal exTaxTotal, Product product, String productName, BigDecimal price, String description, BigDecimal qty,
			Unit unit, VatLine vatLine, Task task) throws AxelorException  {
		
		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, productName, price, description, qty, unit, vatLine, task, product.getInvoiceLineType(), false) {
			@Override
			public List<InvoiceLine> creates() throws AxelorException {
				
				InvoiceLine invoiceLine = this.createInvoiceLine();
				
				List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
				invoiceLines.add(invoiceLine);
				
				return invoiceLines;
			}
		};
		
		return invoiceLineGenerator.creates();
	}
	
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, Task task) throws AxelorException  {
		
		SalesOrderLine salesOrderLine = task.getSalesOrderLine();
		
		if(task.getProduct() != null) {
			return this.createInvoiceLine(invoice, salesOrderLine.getExTaxTotal(), task.getProduct(), task.getProduct().getName(), 
					task.getPrice(), salesOrderLine.getDescription(), task.getQty(), salesOrderLine.getUnit(), salesOrderLine.getVatLine(), salesOrderLine.getTask());
		}
		return this.createInvoiceLine(invoice, salesOrderLine.getExTaxTotal(), salesOrderLine.getProduct(), salesOrderLine.getProductName(), 
				salesOrderLine.getPrice(), salesOrderLine.getDescription(), salesOrderLine.getQty(), salesOrderLine.getUnit(), salesOrderLine.getVatLine(), salesOrderLine.getTask());
	}
	
}
