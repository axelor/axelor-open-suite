package com.axelor.apps.business.project.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.business.project.exception.IExceptionMessage;
import com.axelor.apps.business.project.service.InvoicingProjectService;
import com.axelor.apps.businessproject.db.ElementsToInvoice;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class InvoicingProjectController extends InvoicingProjectRepository{

	@Inject
	protected InvoicingProjectService invoicingProjectService;

	public void generateInvoice(ActionRequest request, ActionResponse response) throws AxelorException{
		InvoicingProject invoicingProject = request.getContext().asType(InvoicingProject.class);
		invoicingProject = this.find(invoicingProject.getId());
		if(invoicingProject.getSaleOrderLineSet().isEmpty() && invoicingProject.getPurchaseOrderLineSet().isEmpty()
				&& invoicingProject.getLogTimesSet().isEmpty() && invoicingProject.getExpenseLineSet().isEmpty() && invoicingProject.getElementsToInvoiceSet().isEmpty()
				&& invoicingProject.getProjectTaskSet().isEmpty()){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_PROJECT_EMPTY)), IException.CONFIGURATION_ERROR);
		}
		if(invoicingProject.getProjectTask() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_PROJECT_PROJECT_TASK)), IException.CONFIGURATION_ERROR);
		}

		if(invoicingProject.getProjectTask().getAssignedTo() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_PROJECT_USER)), IException.CONFIGURATION_ERROR);
		}
		Invoice invoice = invoicingProjectService.generateInvoice(invoicingProject);

		response.setReload(true);
		response.setView(ActionView
				.define("Invoice")
				.model(Invoice.class.getName())
				.add("form", "invoice-form")
				.param("forceEdit", "true")
				.context("_showRecord", String.valueOf(invoice.getId())).map());
	}

	public void fillIn(ActionRequest request, ActionResponse response) throws AxelorException{
		InvoicingProject invoicingProject = request.getContext().asType(InvoicingProject.class);
		ProjectTask projectTask = invoicingProject.getProjectTask();
		if(projectTask == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_PROJECT_PROJECT_TASK)), IException.CONFIGURATION_ERROR);
		}
		List<SaleOrderLine> saleOrderLineList = new ArrayList<SaleOrderLine>();
		List<PurchaseOrderLine> purchaseOrderLineList = new ArrayList<PurchaseOrderLine>();
		List<TimesheetLine> timesheetLineList = new ArrayList<TimesheetLine>();
		List<ExpenseLine> expenseLineList = new ArrayList<ExpenseLine>();
		List<ElementsToInvoice> elementsToInvoiceList = new ArrayList<ElementsToInvoice>();
		List<ProjectTask> projectTaskList = new ArrayList<ProjectTask>();

		invoicingProjectService.getLines(projectTask, saleOrderLineList, purchaseOrderLineList,
				timesheetLineList, expenseLineList, elementsToInvoiceList, projectTaskList, 0);

		invoicingProject.setSaleOrderLineSet(new HashSet<SaleOrderLine>(saleOrderLineList));
		invoicingProject.setPurchaseOrderLineSet(new HashSet<PurchaseOrderLine>(purchaseOrderLineList));
		invoicingProject.setLogTimesSet(new HashSet<TimesheetLine>(timesheetLineList));
		invoicingProject.setExpenseLineSet(new HashSet<ExpenseLine>(expenseLineList));
		invoicingProject.setElementsToInvoiceSet(new HashSet<ElementsToInvoice>(elementsToInvoiceList));
		invoicingProject.setProjectTaskSet(new HashSet<ProjectTask>(projectTaskList));


		response.setValues(invoicingProject);
	}
}
