/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.accountorganisation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.accountorganisation.exceptions.IExceptionMessage;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.organisation.db.Employee;
import com.axelor.apps.organisation.db.ExpenseLine;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.db.Timesheet;
import com.axelor.apps.organisation.db.TimesheetLine;
import com.axelor.apps.organisation.db.repo.ExpenseLineRepository;
import com.axelor.apps.organisation.db.repo.TimesheetLineRepository;
import com.axelor.apps.organisation.service.invoice.InvoiceGeneratorOrganisation;
import com.axelor.apps.organisation.service.invoice.InvoiceLineGeneratorOrganisation;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TaskInvoiceService {
	
	private static final Logger LOG = LoggerFactory.getLogger(TaskInvoiceService.class);
	
	@Inject
	private InvoiceRepository invoiceRepo;
	
	@Inject
	private TimesheetLineRepository timesheetLineRepo;
	
	@Inject
	private ExpenseLineRepository expenseLineRepo;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateInvoice(Task task) throws AxelorException {
		
		Invoice invoice = this.createInvoice(task);
		invoiceRepo.save(invoice);
		return invoice;
	}
			
	public Invoice createInvoice(Task task) throws AxelorException {
		
		Project project = task.getProject();
		
		InvoiceGeneratorOrganisation invoiceGenerator = new InvoiceGeneratorOrganisation(invoiceRepo.OPERATION_TYPE_CLIENT_SALE, project.getCompany(), project.getClientPartner(), 
				project.getContactPartner(), project, null, project.getName(), null) {	
			@Override
			public Invoice generate() throws AxelorException {
				
				return super.createInvoiceHeader();
			}
		};
		
		Invoice invoice = invoiceGenerator.generate();
		invoiceGenerator.populate(invoice, this.createInvoiceLines(invoice, task));
		return invoice;
	}
	
	
	
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, Task task) throws AxelorException  {
		
		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		
		if(task.getIsToInvoice()) {
			this.checkTaskToInvoice(task);
			invoiceLineList.addAll(this.createInvoiceLine(invoice, task));
			task.setIsToInvoice(false);
		}
		
		invoiceLineList.addAll(this.createOtherElementInvoiceLines(invoice, task));
		
		return invoiceLineList;	
	}
	
	
	public void checkTaskToInvoice(Task task) throws AxelorException  {
		if(task.getProduct() == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.TASK_INVOICE_2), IException.CONFIGURATION_ERROR);
		}
		if(task.getQty() == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.TASK_INVOICE_3), IException.CONFIGURATION_ERROR);
		}
		if(task.getPrice() == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.TASK_INVOICE_4), IException.CONFIGURATION_ERROR);
		}
		if(task.getAmountToInvoice() == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.TASK_INVOICE_5), IException.CONFIGURATION_ERROR);
		}
		if(this.computeAmount(task.getQty(), task.getPrice()).compareTo(task.getAmountToInvoice()) != 0)  {
			throw new AxelorException(I18n.get(IExceptionMessage.TASK_INVOICE_6), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	/**
	 * Calculer le montant HT à facturer pour une tache.
	 * 
	 * @param quantity
	 *          Quantité à facturer.
	 * @param price
	 *          Le prix.
	 * 
	 * @return 
	 * 			Le montant HT à facturer.
	 */
	public BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

		BigDecimal amount = quantity.multiply(price).setScale(2, RoundingMode.HALF_EVEN);

		LOG.debug("Calcul du montant HT avec une quantité de {} pour {} : {}", new Object[] { quantity, price, amount });

		return amount;
	}
	
	
	
	public List<InvoiceLine> createOtherElementInvoiceLines(Invoice invoice, Task task) throws AxelorException  {
		
		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		
		invoiceLineList.addAll(this.createTimesheetInvoiceLines(invoice, this.getTimesheetLineToInvoice(task)));	
		
		invoiceLineList.addAll(this.createExpenseInvoiceLines(invoice, this.getExpenseLineToInvoice(task)));	
		
		return invoiceLineList;	
	}
	
	
	public List<TimesheetLine> getTimesheetLineToInvoice(Task task)  {
		
		return (List<TimesheetLine>) timesheetLineRepo.all().filter("self.timesheet.statusSelect = 3 AND self.task.id = ?1 AND self.isToInvoice = true", task.getId()).fetch();
		
	}
	
	
	public List<ExpenseLine> getExpenseLineToInvoice(Task task)  {
		
		return (List<ExpenseLine>) expenseLineRepo.all().filter("self.expense.statusSelect = 3 AND self.task.id = ?1 AND self.isToInvoice = true", task.getId()).fetch();
		
	}
	
	
	public List<InvoiceLine> createExpenseInvoiceLines(Invoice invoice, List<ExpenseLine> expenseLineList) throws AxelorException  {
		
		List<InvoiceLine> expenseInvoiceLineList = new ArrayList<InvoiceLine>();
		
		for(ExpenseLine expenseLine : expenseLineList)  {
			expenseInvoiceLineList.addAll(this.createInvoiceLine(invoice, expenseLine));	
			expenseLine.setIsInvoiced(true);
			expenseLine.setIsToInvoice(false);
		}
		
		return expenseInvoiceLineList;	
	}
	
	
	public List<InvoiceLine> createTimesheetInvoiceLines(Invoice invoice, List<TimesheetLine> timesheetLineList) throws AxelorException  {
		
		List<InvoiceLine> expenseInvoiceLineList = new ArrayList<InvoiceLine>();
		
		for(TimesheetLine timesheetLine  : timesheetLineList)  {
			expenseInvoiceLineList.addAll(this.createInvoiceLine(invoice, timesheetLine));	
			timesheetLine.setIsInvoiced(true);
			timesheetLine.setIsToInvoice(false);
		}
		
		return expenseInvoiceLineList;	
	}
	
	
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, BigDecimal exTaxTotal, Product product, String productName, BigDecimal price, String description, BigDecimal qty,
			Unit unit, TaxLine taxLine, Task task, ProductVariant productVariant) throws AxelorException  {
		
		InvoiceLineGeneratorOrganisation invoiceLineGenerator = new InvoiceLineGeneratorOrganisation(invoice, product, productName, price, description, qty, unit, taxLine, task, 
				product.getInvoiceLineType(), BigDecimal.ZERO, 0, exTaxTotal, false) {
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
		
		return this.createInvoiceLine(invoice, task.getAmountToInvoice(), task.getProduct(), task.getProduct().getName(), 
				task.getPrice(), task.getDescription(), task.getQty(), task.getProduct().getUnit(), null, 
				task, null);
		
	}
	
	
	//TODO montant TTC : opérationnel une fois l'ajout d'un TTC sur la facture réalisé
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, ExpenseLine expenseLine) throws AxelorException  {
		
		return this.createInvoiceLine(invoice, expenseLine.getInTaxTotal(), expenseLine.getProduct(), expenseLine.getProduct().getName(), 
				expenseLine.getPrice(), expenseLine.getShortDescription(), expenseLine.getQty(), expenseLine.getProduct().getUnit(), expenseLine.getTaxLine(), 
				expenseLine.getTask(), null);
		
	}
	
	
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, TimesheetLine timesheetLine) throws AxelorException  {
		
		Timesheet timesheet = timesheetLine.getTimesheet();
		
		Product product = this.getTimesheetProduct(this.getTimesheetEmployee(timesheet));
		
		return this.createInvoiceLine(invoice, null, product, product.getName(), 
				product.getSalePrice(), timesheetLine.getTask().getDescription(), timesheetLine.getDuration(), timesheet.getUnit(), null, 
				timesheetLine.getTask(), null);
		
	}
	
	
	public Employee getTimesheetEmployee(Timesheet timesheet) throws AxelorException  {
		
		Employee employee = timesheet.getUser().getEmployee();
		
		if(employee == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TASK_INVOICE_1), timesheet.getUser().getFullName()), IException.CONFIGURATION_ERROR);
		}
		
		return employee;
		
	}
	
	
	public Product getTimesheetProduct(Employee employee) throws AxelorException  {
		
		Product profileProduct = employee.getProfileProduct();
		
		if(profileProduct == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TASK_INVOICE_7), employee.getName(), employee.getFirstName()), IException.CONFIGURATION_ERROR);
		}
		
		return profileProduct;
			
	}
	
}
