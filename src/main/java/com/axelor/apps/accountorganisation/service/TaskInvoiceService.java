package com.axelor.apps.accountorganisation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public class TaskInvoiceService {
	
	private static final Logger LOG = LoggerFactory.getLogger(TaskInvoiceService.class);
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateInvoice(Task task) throws AxelorException {
		
		// Check if the field task.isToInvoice = true
		if(task.getIsToInvoice()) {
			
			SalesOrderLine salesOrderLine = task.getSalesOrderLine();
			// Check if task.salesOrderLine and task.salesOrderLine.salesOrder are not empty
			if(salesOrderLine != null && salesOrderLine.getSalesOrder() != null) {
				
				Invoice invoice = this.createInvoice(task);
				invoice.save();
				return invoice;
			}
		}
		return null;
	}
			
	public Invoice createInvoice(Task task) throws AxelorException {
		
		SalesOrder salesOrder = task.getSalesOrderLine().getSalesOrder();
		
		InvoiceGenerator invoiceGenerator = new InvoiceGenerator(IInvoice.CLIENT_SALE, salesOrder.getCompany(),salesOrder.getPaymentCondition(), 
				salesOrder.getPaymentMode(), salesOrder.getMainInvoicingAddress(), salesOrder.getClientPartner(), salesOrder.getContactPartner(), salesOrder.getCurrency()) {
			
			@Override
			public Invoice generate() throws AxelorException {
				
				return super.createInvoiceHeader();
			}
		};
		
		Invoice invoice = invoiceGenerator.generate();
		invoiceGenerator.populate(invoice, this.createInvoiceLines(invoice, task));
		invoiceGenerator.computeInvoice(invoice);
		return invoice;
	}
	
	@Transactional
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, Task task) throws AxelorException  {
		
		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		
		invoiceLineList.addAll(this.createInvoiceLine(invoice, task));	
		
		return invoiceLineList;	
	}
	
	
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, BigDecimal exTaxTotal, Product product, String productName, BigDecimal price, String description, BigDecimal qty,
			Unit unit, VatLine vatLine, Task task, ProductVariant productVariant) throws AxelorException  {
		
		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, productName, price, description, qty, unit, vatLine, task, 
				product.getInvoiceLineType(), productVariant, false) {
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
					task.getPrice(), salesOrderLine.getDescription(), task.getQty(), salesOrderLine.getUnit(), salesOrderLine.getVatLine(), 
					task, salesOrderLine.getProductVariant());
		}
		return this.createInvoiceLine(invoice, salesOrderLine.getExTaxTotal(), salesOrderLine.getProduct(), salesOrderLine.getProductName(), 
				salesOrderLine.getPrice(), salesOrderLine.getDescription(), salesOrderLine.getQty(), salesOrderLine.getUnit(), salesOrderLine.getVatLine(), 
				task, salesOrderLine.getProductVariant());
	}
	
}
