package com.axelor.apps.business.project.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.business.project.exception.IExceptionMessage;
import com.axelor.apps.business.project.service.InvoicingFolderService;
import com.axelor.apps.businessproject.db.InvoicingFolder;
import com.axelor.apps.businessproject.db.repo.InvoicingFolderRepository;
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

public class InvoicingFolderController extends InvoicingFolderRepository{

	@Inject
	protected InvoicingFolderService invoicingFolderService;

	public void generateInvoice(ActionRequest request, ActionResponse response) throws AxelorException{
		InvoicingFolder folder = request.getContext().asType(InvoicingFolder.class);
		folder = this.find(folder.getId());
		if(folder.getSaleOrderLineSet().isEmpty() && folder.getPurchaseOrderLineSet().isEmpty()
				&& folder.getLogTimesSet().isEmpty() && folder.getExpenseLineSet().isEmpty() && folder.getAnalyticMoveLineSet().isEmpty()
				&& folder.getProjectTaskSet().isEmpty()){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_FOLDER_EMPTY)), IException.CONFIGURATION_ERROR);
		}
		if(folder.getProjectTask() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_FOLDER_PROJECT_TASK)), IException.CONFIGURATION_ERROR);
		}
		if(folder.getProjectTask().getCustomer() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_FOLDER_CUSTOMER)), IException.CONFIGURATION_ERROR);
		}

		if(folder.getProjectTask().getAssignedTo() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_FOLDER_USER)), IException.CONFIGURATION_ERROR);
		}
		Invoice invoice = invoicingFolderService.generateInvoice(folder);

		response.setReload(true);
		response.setView(ActionView
				.define("Invoice")
				.model(Invoice.class.getName())
				.add("form", "invoice-form")
				.param("forceEdit", "true")
				.context("_showRecord", String.valueOf(invoice.getId())).map());
	}

	public void fillIn(ActionRequest request, ActionResponse response) throws AxelorException{
		InvoicingFolder invoicingFolder = request.getContext().asType(InvoicingFolder.class);
		ProjectTask projectTask = invoicingFolder.getProjectTask();
		if(projectTask == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_FOLDER_PROJECT_TASK)), IException.CONFIGURATION_ERROR);
		}
		List<SaleOrderLine> saleOrderLineList = new ArrayList<SaleOrderLine>();
		List<PurchaseOrderLine> purchaseOrderLineList = new ArrayList<PurchaseOrderLine>();
		List<TimesheetLine> timesheetLineList = new ArrayList<TimesheetLine>();
		List<ExpenseLine> expenseLineList = new ArrayList<ExpenseLine>();
		List<AnalyticMoveLine> analyticMoveLineList = new ArrayList<AnalyticMoveLine>();
		List<ProjectTask> projectTaskList = new ArrayList<ProjectTask>();

		invoicingFolderService.getLines(projectTask, saleOrderLineList, purchaseOrderLineList,
				timesheetLineList, expenseLineList, analyticMoveLineList, projectTaskList);


		invoicingFolder.setSaleOrderLineSet(new HashSet<SaleOrderLine>(saleOrderLineList));
		invoicingFolder.setPurchaseOrderLineSet(new HashSet<PurchaseOrderLine>(purchaseOrderLineList));
		invoicingFolder.setLogTimesSet(new HashSet<TimesheetLine>(timesheetLineList));
		invoicingFolder.setExpenseLineSet(new HashSet<ExpenseLine>(expenseLineList));
		invoicingFolder.setAnalyticMoveLineSet(new HashSet<AnalyticMoveLine>(analyticMoveLineList));
		invoicingFolder.setProjectTaskSet(new HashSet<ProjectTask>(projectTaskList));


		response.setValues(invoicingFolder);
	}
}
