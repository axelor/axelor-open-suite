package com.axelor.apps.business.project.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.business.project.exception.IExceptionMessage;
import com.axelor.apps.businessproject.db.ElementsToInvoice;
import com.axelor.apps.businessproject.db.InvoicingFolder;
import com.axelor.apps.businessproject.db.repo.ElementsToInvoiceRepository;
import com.axelor.apps.businessproject.db.repo.InvoicingFolderRepository;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImp;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InvoicingFolderService extends InvoicingFolderRepository{


	@Inject
	protected SaleOrderInvoiceServiceImpl saleOrderInvoiceServiceImpl;

	@Inject
	protected PurchaseOrderInvoiceServiceImpl purchaseOrderInvoiceServiceImpl;

	@Inject
	protected TimesheetServiceImp timesheetServiceImp;

	@Inject
	protected ExpenseService expenseService;

	@Inject
	protected ElementsToInvoiceService elementsToInvoiceService;

	protected int MAX_LEVEL_OF_PROJECT = 10;


	@Transactional
	public Invoice generateInvoice(InvoicingFolder folder) throws AxelorException{
		ProjectTask projectTask = folder.getProjectTask();
		Partner customer = projectTask.getCustomer();
		Company company = this.getRootCompany(projectTask);
		if(company == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_FOLDER_PROJECT_TASK_COMPANY)), IException.CONFIGURATION_ERROR);
		}
		User user = projectTask.getAssignedTo();
		InvoiceGenerator invoiceGenerator = new InvoiceGenerator(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE, company, customer.getPaymentCondition(),
				customer.getPaymentMode(), customer.getMainInvoicingAddress(), customer, null,
				customer.getCurrency(), customer.getSalePriceList(), null, null){

			@Override
			public Invoice generate() throws AxelorException {

				return super.createInvoiceHeader();
			}
		};
		Invoice invoice = invoiceGenerator.generate();
		invoice.setInAti(user.getActiveCompany().getAccountConfig().getInvoiceInAti());
		invoiceGenerator.populate(invoice,this.populate(invoice,folder));
		Beans.get(InvoiceRepository.class).save(invoice);

		this.setInvoiced(folder);
		folder.setInvoice(invoice);
		save(folder);
		return invoice;
	}

	public List<InvoiceLine> populate(Invoice invoice,InvoicingFolder folder) throws AxelorException{
		List<SaleOrderLine> saleOrderLineList = new ArrayList<SaleOrderLine>(folder.getSaleOrderLineSet());
		List<PurchaseOrderLine> purchaseOrderLineList = new ArrayList<PurchaseOrderLine>(folder.getPurchaseOrderLineSet());
		List<TimesheetLine> timesheetLineList = new ArrayList<TimesheetLine>(folder.getLogTimesSet());
		List<ExpenseLine> expenseLineList = new ArrayList<ExpenseLine>(folder.getExpenseLineSet());
		List<ElementsToInvoice> elementsToInvoiceList = new ArrayList<ElementsToInvoice>(folder.getElementsToInvoiceSet());
		List<ProjectTask> projectTaskList = new ArrayList<ProjectTask>(folder.getProjectTaskSet());

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		invoiceLineList.addAll( saleOrderInvoiceServiceImpl.createInvoiceLines(invoice, saleOrderLineList));
		invoiceLineList.addAll(this.customerChargeBackPurchases(purchaseOrderInvoiceServiceImpl.createInvoiceLines(invoice, purchaseOrderLineList),folder));
		invoiceLineList.addAll(timesheetServiceImp.createInvoiceLines(invoice, timesheetLineList));
		invoiceLineList.addAll(this.customerChargeBackExpenses(expenseService.createInvoiceLines(invoice, expenseLineList),folder));
		invoiceLineList.addAll(elementsToInvoiceService.createInvoiceLines(invoice, elementsToInvoiceList));
		invoiceLineList.addAll(this.createInvoiceLines(invoice, projectTaskList));

		for (InvoiceLine invoiceLine : invoiceLineList) {
			invoiceLine.setSaleOrder(invoiceLine.getInvoice().getSaleOrder());
		}

		return invoiceLineList;
	}


	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<ProjectTask> projectTaskList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for(ProjectTask projectTask : projectTaskList)  {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, projectTask));

		}

		return invoiceLineList;

	}

	public List<InvoiceLine> createInvoiceLine(Invoice invoice, ProjectTask projectTask) throws AxelorException  {

		Product product = projectTask.getProduct();

		if(product == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_FOLDER_PROJECT_TASK_PRODUCT),projectTask.getFullName()), IException.CONFIGURATION_ERROR);
		}

		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, product.getName(), projectTask.getPrice(),
				null,projectTask.getQty(),projectTask.getUnit(),10,BigDecimal.ZERO,IPriceListLine.AMOUNT_TYPE_NONE,
				projectTask.getPrice().multiply(projectTask.getQty()),null,false)  {

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

	public void setInvoiced(InvoicingFolder folder){
		for (SaleOrderLine saleOrderLine : folder.getSaleOrderLineSet()) {
			saleOrderLine.setInvoiced(true);
		}
		for (PurchaseOrderLine purchaseOrderLine : folder.getPurchaseOrderLineSet()) {
			purchaseOrderLine.setInvoiced(true);
		}
		for (TimesheetLine timesheetLine : folder.getLogTimesSet()) {
			timesheetLine.setInvoiced(true);
		}
		for (ExpenseLine expenseLine : folder.getExpenseLineSet()) {
			expenseLine.setInvoiced(true);
		}
		for (ElementsToInvoice elementsToInvoice : folder.getElementsToInvoiceSet()) {
			elementsToInvoice.setInvoiced(true);
		}
		for (ProjectTask projectTask : folder.getProjectTaskSet()) {
			projectTask.setInvoiced(true);
		}
	}


	public List<InvoiceLine> customerChargeBackPurchases(List<InvoiceLine> invoiceLineList,InvoicingFolder folder){
		Partner customer = folder.getProjectTask().getCustomer();
		if(!customer.getFlatFeeExpense()){
			for (InvoiceLine invoiceLine : invoiceLineList) {
				invoiceLine.setPrice(invoiceLine.getPrice().multiply(customer.getChargeBackPurchase().divide(new BigDecimal(100), GeneralService.getNbDecimalDigitForUnitPrice(), BigDecimal.ROUND_HALF_UP)));
				invoiceLine.setExTaxTotal(invoiceLine.getPrice().multiply(invoiceLine.getQty()).setScale(2, BigDecimal.ROUND_HALF_UP));
			}
		}
		return invoiceLineList;
	}

	public List<InvoiceLine> customerChargeBackExpenses(List<InvoiceLine> invoiceLineList,InvoicingFolder folder){
		Partner customer = folder.getProjectTask().getCustomer();
		if(!customer.getFlatFeePurchase()){
			for (InvoiceLine invoiceLine : invoiceLineList) {
				invoiceLine.setPrice(invoiceLine.getPrice().multiply(customer.getChargeBackExpense().divide(new BigDecimal(100), GeneralService.getNbDecimalDigitForUnitPrice(), BigDecimal.ROUND_HALF_UP)));
				invoiceLine.setExTaxTotal(invoiceLine.getPrice().multiply(invoiceLine.getQty()).setScale(2, BigDecimal.ROUND_HALF_UP));
			}
		}
		return invoiceLineList;
	}


	public void getLines(ProjectTask projectTask, List<SaleOrderLine> saleOrderLineList, List<PurchaseOrderLine> purchaseOrderLineList,
							List<TimesheetLine> timesheetLineList,  List<ExpenseLine> expenseLineList, List<ElementsToInvoice> elementsToInvoiceList, List<ProjectTask> projectTaskList, int counter){

		if(counter > MAX_LEVEL_OF_PROJECT)  {  return;  }
		counter++;

		saleOrderLineList.addAll(Beans.get(SaleOrderLineRepository.class).all().filter("self.project != null AND self.project = ?1 AND self.toInvoice = true AND self.invoiced = false AND self.project.invoicingTypeSelect = ?2", projectTask, ProjectTaskRepository.INVOICING_TYPE_TIME_BASED).fetch());
		purchaseOrderLineList.addAll(Beans.get(PurchaseOrderLineRepository.class).all().filter("self.projectTask != null AND self.projectTask = ?1 AND self.projectTask.saleOrder != null  AND self.toInvoice = true AND self.invoiced = false AND self.projectTask.invoicingTypeSelect = ?2", projectTask,  ProjectTaskRepository.INVOICING_TYPE_TIME_BASED).fetch());
		timesheetLineList.addAll(Beans.get(TimesheetLineRepository.class).all().filter("self.affectedToTimeSheet != null AND self.affectedToTimeSheet.statusSelect = 3 AND self.projectTask = ?1 AND self.projectTask.saleOrder != null AND self.toInvoice = true AND self.invoiced = false AND self.projectTask.invoicingTypeSelect = ?2", projectTask, ProjectTaskRepository.INVOICING_TYPE_TIME_BASED).fetch());
		expenseLineList.addAll(Beans.get(ExpenseLineRepository.class).all().filter("self.task != null AND self.task = ?1 AND self.task.saleOrder != null AND self.toInvoice = true AND self.invoiced = false AND self.task.invoicingTypeSelect = ?2", projectTask, ProjectTaskRepository.INVOICING_TYPE_TIME_BASED).fetch());
		elementsToInvoiceList.addAll(Beans.get(ElementsToInvoiceRepository.class).all().filter("self.project != null AND self.project = ?1 AND self.project.saleOrder != null AND self.toInvoice = true AND self.invoiced = false AND self.project.invoicingTypeSelect = ?2", projectTask, ProjectTaskRepository.INVOICING_TYPE_TIME_BASED).fetch());
		projectTaskList.addAll(Beans.get(ProjectTaskRepository.class).all().filter("self.id = ?1 AND self.saleOrder != null AND self.invoicingTypeSelect = ?2 AND self.invoiced = false", projectTask.getId(), ProjectTaskRepository.INVOICING_TYPE_FLAT_RATE).fetch());
		List<ProjectTask> projectTaskChildrenList = Beans.get(ProjectTaskRepository.class).all().filter("self.project = ?1 AND (self.invoicingTypeSelect = ?2 OR self.invoicingTypeSelect = ?3) ", projectTask, ProjectTaskRepository.INVOICING_TYPE_TIME_BASED, ProjectTaskRepository.INVOICING_TYPE_FLAT_RATE).fetch();
		for (ProjectTask projectTaskChild : projectTaskChildrenList) {
			this.getLines(projectTaskChild, saleOrderLineList, purchaseOrderLineList,
					timesheetLineList, expenseLineList, elementsToInvoiceList, projectTaskList, counter);
		}
		return;
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
