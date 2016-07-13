/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.account.util.InvoiceLineComparator;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.businessproject.db.ElementsToInvoice;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.ElementsToInvoiceRepository;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImpl;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineGeneratorSupplyChain;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InvoicingProjectService {

	@Inject
	protected SaleOrderInvoiceServiceImpl saleOrderInvoiceServiceImpl;

	@Inject
	protected PurchaseOrderInvoiceServiceImpl purchaseOrderInvoiceServiceImpl;

	@Inject
	protected TimesheetServiceImpl timesheetServiceImpl;

	@Inject
	protected ExpenseService expenseService;

	@Inject
	protected ElementsToInvoiceService elementsToInvoiceService;

	protected AnalyticMoveLineService analyticMoveLineService;

	@Inject
	protected PartnerService partnerService;
	
	@Inject
	protected InvoicingProjectRepository  invoicingProjectRepo;

	protected int MAX_LEVEL_OF_PROJECT = 10;

	protected int sequence = 0;

	@Transactional
	public Invoice generateInvoice(InvoicingProject invoicingProject) throws AxelorException{
		ProjectTask projectTask = invoicingProject.getProjectTask();
		Partner customer = projectTask.getClientPartner();
		Company company = this.getRootCompany(projectTask);
		if(company == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_PROJECT_PROJECT_TASK_COMPANY)), IException.CONFIGURATION_ERROR);
		}
		projectTask.getAssignedTo();
		InvoiceGenerator invoiceGenerator = new InvoiceGenerator(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE, company, customer.getPaymentCondition(),
				customer.getPaymentMode(), partnerService.getInvoicingAddress(customer), customer, null,
				customer.getCurrency(), customer.getSalePriceList(), null, null, null, null){

			@Override
			public Invoice generate() throws AxelorException {

				return super.createInvoiceHeader();
			}
		};
		Invoice invoice = invoiceGenerator.generate();

		invoiceGenerator.populate(invoice,this.populate(invoice,invoicingProject));
		Beans.get(InvoiceRepository.class).save(invoice);

		this.setInvoiced(invoicingProject);
		invoicingProject.setInvoice(invoice);
		invoicingProjectRepo.save(invoicingProject);
		return invoice;
	}

	public List<InvoiceLine> populate(Invoice invoice,InvoicingProject folder) throws AxelorException{
		List<SaleOrderLine> saleOrderLineList = new ArrayList<SaleOrderLine>(folder.getSaleOrderLineSet());
		List<PurchaseOrderLine> purchaseOrderLineList = new ArrayList<PurchaseOrderLine>(folder.getPurchaseOrderLineSet());
		List<TimesheetLine> timesheetLineList = new ArrayList<TimesheetLine>(folder.getLogTimesSet());
		List<ExpenseLine> expenseLineList = new ArrayList<ExpenseLine>(folder.getExpenseLineSet());
		List<ElementsToInvoice> elementsToInvoiceList = new ArrayList<ElementsToInvoice>(folder.getElementsToInvoiceSet());
		List<ProjectTask> projectTaskList = new ArrayList<ProjectTask>(folder.getProjectTaskSet());

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		invoiceLineList.addAll(this.createSaleOrderInvoiceLines(invoice, saleOrderLineList, folder.getSaleOrderLineSetPrioritySelect()));
		invoiceLineList.addAll(this.createPurchaseOrderInvoiceLines(invoice, purchaseOrderLineList, folder.getPurchaseOrderLineSetPrioritySelect()));
		invoiceLineList.addAll(timesheetServiceImpl.createInvoiceLines(invoice, timesheetLineList, folder.getLogTimesSetPrioritySelect()));
		invoiceLineList.addAll(expenseService.createInvoiceLines(invoice, expenseLineList, folder.getExpenseLineSetPrioritySelect()));
		invoiceLineList.addAll(elementsToInvoiceService.createInvoiceLines(invoice, elementsToInvoiceList, folder.getElementsToInvoiceSetPrioritySelect()));
		invoiceLineList.addAll(this.createInvoiceLines(invoice, projectTaskList, folder.getProjectTaskSetPrioritySelect()));

		Collections.sort(invoiceLineList, new InvoiceLineComparator());

		for (InvoiceLine invoiceLine : invoiceLineList) {
			invoiceLine.setSequence(sequence);
			sequence++;
			invoiceLine.setSaleOrder(invoiceLine.getInvoice().getSaleOrder());
		}

		return invoiceLineList;
	}


	public List<InvoiceLine> createSaleOrderInvoiceLines(Invoice invoice, List<SaleOrderLine> saleOrderLineList, int priority) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		int count = 1;
		for(SaleOrderLine saleOrderLine : saleOrderLineList)  {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, saleOrderLine,priority*100+count));
			count++;
			saleOrderLine.setInvoiced(true);
		}

		return invoiceLineList;

	}


	public List<InvoiceLine> createInvoiceLine(Invoice invoice, SaleOrderLine saleOrderLine, int priority) throws AxelorException  {

		Product product = saleOrderLine.getProduct();

		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGeneratorSupplyChain(invoice, product, saleOrderLine.getProductName(),
				saleOrderLine.getDescription(), saleOrderLine.getQty(), saleOrderLine.getUnit(),
				priority, false, saleOrderLine, null, null, null)  {

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

	public List<InvoiceLine> createPurchaseOrderInvoiceLines(Invoice invoice, List<PurchaseOrderLine> purchaseOrderLineList, int priority) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		for(PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {

			invoiceLineList.addAll(Beans.get(PurchaseOrderInvoiceProjectServiceImpl.class).createInvoiceLine(invoice, purchaseOrderLine));
			purchaseOrderLine.setInvoiced(true);
		}
		return invoiceLineList;
	}


	public List<InvoiceLine> createInvoiceLine(Invoice invoice, PurchaseOrderLine purchaseOrderLine, int priority) throws AxelorException  {

		Product product = purchaseOrderLine.getProduct();

		InvoiceLineGeneratorSupplyChain invoiceLineGenerator = new InvoiceLineGeneratorSupplyChain(invoice, product, purchaseOrderLine.getProductName(),
				purchaseOrderLine.getDescription(), purchaseOrderLine.getQty(), purchaseOrderLine.getUnit(),
				priority, false, null, purchaseOrderLine, null)  {
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

	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<ProjectTask> projectTaskList, int priority) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		int count = 0;
		for(ProjectTask projectTask : projectTaskList)  {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, projectTask, priority*100+count));
			count++;
			projectTask.setInvoiced(true);
			invoiceLineList.get(invoiceLineList.size()-1).setProject(projectTask);
		}

		return invoiceLineList;

	}

	public List<InvoiceLine> createInvoiceLine(Invoice invoice, ProjectTask projectTask, int priority) throws AxelorException  {

		Product product = projectTask.getProduct();

		if(product == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_PROJECT_PROJECT_TASK_PRODUCT),projectTask.getFullName()), IException.CONFIGURATION_ERROR);
		}

		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, projectTask.getName(), projectTask.getPrice(),
					projectTask.getPrice(),null,projectTask.getQty(),projectTask.getUnit(), null,priority,BigDecimal.ZERO,IPriceListLine.AMOUNT_TYPE_NONE,
					projectTask.getPrice().multiply(projectTask.getQty()), null,false)  {

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

	public void setInvoiced(InvoicingProject invoicingProject){
		for (SaleOrderLine saleOrderLine : invoicingProject.getSaleOrderLineSet()) {
			saleOrderLine.setInvoiced(true);
		}
		for (PurchaseOrderLine purchaseOrderLine : invoicingProject.getPurchaseOrderLineSet()) {
			purchaseOrderLine.setInvoiced(true);
		}
		for (TimesheetLine timesheetLine : invoicingProject.getLogTimesSet()) {
			timesheetLine.setInvoiced(true);
		}
		for (ExpenseLine expenseLine : invoicingProject.getExpenseLineSet()) {
			expenseLine.setInvoiced(true);
		}
		for (ElementsToInvoice elementsToInvoice : invoicingProject.getElementsToInvoiceSet()) {
			elementsToInvoice.setInvoiced(true);
		}
		for (ProjectTask projectTask : invoicingProject.getProjectTaskSet()) {
			projectTask.setInvoiced(true);
		}
	}


	public void setLines(InvoicingProject invoicingProject,ProjectTask projectTask, int counter){
		
		
		if(counter > ProjectTaskService.MAX_LEVEL_OF_PROJECT)  {  return;  }
		counter++;
		
		this.fillLines(invoicingProject, projectTask);

		List<ProjectTask> projectTaskChildrenList = Beans.get(ProjectTaskRepository.class).all().filter("self.project = ?1", projectTask).fetch();

		for (ProjectTask projectTaskChild : projectTaskChildrenList) {
			this.setLines(invoicingProject, projectTaskChild, counter);
		}

		return;
	}
	
	public void fillLines(InvoicingProject invoicingProject,ProjectTask projectTask){
		if(projectTask.getProjTaskInvTypeSelect() == ProjectTaskRepository.INVOICING_TYPE_FLAT_RATE || projectTask.getProjTaskInvTypeSelect() == ProjectTaskRepository.INVOICING_TYPE_TIME_BASED)  {

			invoicingProject.getSaleOrderLineSet().addAll(Beans.get(SaleOrderLineRepository.class)
					.all().filter("self.saleOrder.project = ?1 AND self.toInvoice = true AND self.invoiced = false AND (self.saleOrder.creationDate < ?2 or ?3 is null)", projectTask, invoicingProject.getDeadlineDate(),invoicingProject.getDeadlineDate()).fetch());
			
			invoicingProject.getPurchaseOrderLineSet().addAll(Beans.get(PurchaseOrderLineRepository.class)
					.all().filter("self.projectTask = ?1 AND self.toInvoice = true AND self.invoiced = false AND (self.purchaseOrder.orderDate < ?2 or ?3 is null)", projectTask, invoicingProject.getDeadlineDate(),invoicingProject.getDeadlineDate()).fetch());
			
			invoicingProject.getLogTimesSet().addAll(Beans.get(TimesheetLineRepository.class)
					.all().filter("self.affectedToTimeSheet.statusSelect = 3 AND self.projectTask = ?1 AND self.toInvoice = true AND self.invoiced = false AND (self.date < ?2 or ?3 is null)", projectTask, invoicingProject.getDeadlineDate(),invoicingProject.getDeadlineDate()).fetch());
			
			invoicingProject.getExpenseLineSet().addAll(Beans.get(ExpenseLineRepository.class)
					.all().filter("self.projectTask = ?1 AND self.toInvoice = true AND self.invoiced = false AND (self.expenseDate < ?2 or ?3 is null)", projectTask, invoicingProject.getDeadlineDate(),invoicingProject.getDeadlineDate()).fetch());
			
			invoicingProject.getElementsToInvoiceSet().addAll(Beans.get(ElementsToInvoiceRepository.class)
					.all().filter("self.project = ?1 AND self.toInvoice = true AND self.invoiced = false AND (self.date < ?2 or ?3 is null)", projectTask, invoicingProject.getDeadlineDate(),invoicingProject.getDeadlineDate()).fetch());
			
			if(projectTask.getProjTaskInvTypeSelect() == ProjectTaskRepository.INVOICING_TYPE_FLAT_RATE && !projectTask.getInvoiced())
				invoicingProject.addProjectTaskSetItem(projectTask);
		}
	}
	
	public void clearLines(InvoicingProject invoicingProject){
		
		invoicingProject.clearSaleOrderLineSet();
		invoicingProject.clearPurchaseOrderLineSet();
		invoicingProject.clearLogTimesSet();
		invoicingProject.clearExpenseLineSet();
		invoicingProject.clearElementsToInvoiceSet();
		invoicingProject.clearProjectTaskSet();
	}
	
	
	public Company getRootCompany(ProjectTask projectTask){
		if(projectTask.getProject() == null){
			return projectTask.getCompany();
		}
		else{
			return getRootCompany(projectTask.getProject());
		}
	}
}
