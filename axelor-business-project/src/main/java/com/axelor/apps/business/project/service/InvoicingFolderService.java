package com.axelor.apps.business.project.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.businessproject.db.BusinessFolder;
import com.axelor.apps.businessproject.db.InvoicingFolder;
import com.axelor.apps.businessproject.db.repo.InvoicingFolderRepository;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImp;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
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
	protected AnalyticMoveLineService analyticMoveLineService;


	@Transactional
	public Invoice generateInvoice(InvoicingFolder folder) throws AxelorException{
		BusinessFolder businessFolder = folder.getBusinessFolder();
		Partner customer = businessFolder.getCustomer();
		User user = businessFolder.getUserResponsible();
		InvoiceGenerator invoiceGenerator = new InvoiceGenerator(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE, user.getActiveCompany(),customer.getPaymentCondition(),
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
		List<AnalyticMoveLine> analyticMoveLineList = new ArrayList<AnalyticMoveLine>(folder.getAnalyticMoveLineSet());
		List<ProjectTask> projectTaskList = new ArrayList<ProjectTask>(folder.getProjectTaskSet());

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		invoiceLineList.addAll( saleOrderInvoiceServiceImpl.createInvoiceLines(invoice, saleOrderLineList));
		invoiceLineList.addAll(this.customerChargeBackPurchases(purchaseOrderInvoiceServiceImpl.createInvoiceLines(invoice, purchaseOrderLineList),folder));
		invoiceLineList.addAll(timesheetServiceImp.createInvoiceLines(invoice, timesheetLineList));
		invoiceLineList.addAll(this.customerChargeBackExpenses(expenseService.createInvoiceLines(invoice, expenseLineList),folder));
		invoiceLineList.addAll(this.customerChargeBackAnalytics(analyticMoveLineService.createInvoiceLines(invoice, analyticMoveLineList),folder));
		invoiceLineList.addAll(this.createInvoiceLines(invoice, projectTaskList));

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

		Product product = null;//projectTask.getProduct();

		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, product.getName(),
				null,BigDecimal.ONE, product.getUnit(),10,false)  {

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
		for (AnalyticMoveLine analyticMoveLine : folder.getAnalyticMoveLineSet()) {
			analyticMoveLine.setInvoiced(true);
		}
		for (ProjectTask projectTask : folder.getProjectTaskSet()) {
			projectTask.setInvoiced(true);
		}
	}


	public List<InvoiceLine> customerChargeBackPurchases(List<InvoiceLine> invoiceLineList,InvoicingFolder folder){
		Partner customer = folder.getBusinessFolder().getCustomer();
		if(!customer.getFlatFeeExpense()){
			for (InvoiceLine invoiceLine : invoiceLineList) {
				invoiceLine.setPrice(invoiceLine.getPrice().multiply(customer.getChargeBackPurchase().divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP)));
				invoiceLine.setExTaxTotal(invoiceLine.getPrice().multiply(invoiceLine.getQty()).setScale(2, BigDecimal.ROUND_HALF_UP));
			}
		}
		return invoiceLineList;
	}

	public List<InvoiceLine> customerChargeBackExpenses(List<InvoiceLine> invoiceLineList,InvoicingFolder folder){
		Partner customer = folder.getBusinessFolder().getCustomer();
		if(!customer.getFlatFeePurchase()){
			for (InvoiceLine invoiceLine : invoiceLineList) {
				invoiceLine.setPrice(invoiceLine.getPrice().multiply(customer.getChargeBackExpense().divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP)));
				invoiceLine.setExTaxTotal(invoiceLine.getPrice().multiply(invoiceLine.getQty()).setScale(2, BigDecimal.ROUND_HALF_UP));
			}
		}
		return invoiceLineList;
	}

	public List<InvoiceLine> customerChargeBackAnalytics(List<InvoiceLine> invoiceLineList,InvoicingFolder folder){
		Partner customer = folder.getBusinessFolder().getCustomer();
		if(!customer.getFlatFeeAnalytic()){
			for (InvoiceLine invoiceLine : invoiceLineList) {
				invoiceLine.setPrice(invoiceLine.getPrice().multiply(customer.getChargeBackAnanlytic().divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP)));
				invoiceLine.setExTaxTotal(invoiceLine.getPrice().multiply(invoiceLine.getQty()).setScale(2, BigDecimal.ROUND_HALF_UP));
			}
		}
		return invoiceLineList;
	}
}
