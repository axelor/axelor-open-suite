package com.axelor.apps.business.project.web;

import java.util.HashSet;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.business.project.exception.IExceptionMessage;
import com.axelor.apps.business.project.service.InvoicingFolderService;
import com.axelor.apps.businessproject.db.BusinessFolder;
import com.axelor.apps.businessproject.db.InvoicingFolder;
import com.axelor.apps.businessproject.db.repo.InvoicingFolderRepository;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
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
				&& folder.getLogTimesSet().isEmpty() && folder.getExpenseLineSet().isEmpty() && folder.getAnalyticMoveLineSet().isEmpty()){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_FOLDER_EMPTY)), IException.CONFIGURATION_ERROR);
		}
		if(folder.getBusinessFolder() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_FOLDER_FOLDER)), IException.CONFIGURATION_ERROR);
		}
		if(folder.getBusinessFolder().getCustomer() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_FOLDER_CUSTOMER)), IException.CONFIGURATION_ERROR);
		}

		if(folder.getBusinessFolder().getUserResponsible() == null){
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
		BusinessFolder folder = invoicingFolder.getBusinessFolder();
		invoicingFolder.setSaleOrderLineSet(new HashSet(Beans.get(SaleOrderLineRepository.class).all().filter("self.project != null AND (self.project.businessFolder = ?1 OR self.project.project.businessFolder = ?1) AND self.invoiceable = true AND self.invoiced = false AND (self.project.imputable = true OR self.project.project.imputable = true)", folder).fetch()));
		invoicingFolder.setPurchaseOrderLineSet(new HashSet(Beans.get(PurchaseOrderLineRepository.class).all().filter("self.projectTask != null AND (self.projectTask.businessFolder = ?1 OR self.projectTask.project.businessFolder = ?1) AND (self.projectTask.saleOrder != null OR self.projectTask.project.saleOrder != null) AND self.invoiceable = true AND self.invoiced = false AND (self.projectTask.imputable = true OR self.projectTask.project.imputable = true)", folder).fetch()));
		invoicingFolder.setLogTimesSet(new HashSet(Beans.get(TimesheetLineRepository.class).all().filter("self.affectedToTimeSheet != null AND self.affectedToTimeSheet.statusSelect = 3 AND (self.projectTask.businessFolder = ?1 OR self.projectTask.project.businessFolder = ?1) AND (self.projectTask.saleOrder != null OR self.projectTask.project.saleOrder != null) AND self.invoiceable = true AND self.invoiced = false AND (self.projectTask.imputable = true OR self.projectTask.project.imputable = true)", folder).fetch()));
		invoicingFolder.setExpenseLineSet(new HashSet(Beans.get(ExpenseLineRepository.class).all().filter("self.task != null AND (self.task.businessFolder = ?1 OR self.task.project.businessFolder = ?1) AND (self.task.saleOrder != null OR self.task.project.saleOrder != null) AND self.invoiceable = true AND self.invoiced = false AND (self.task.imputable = true OR self.task.project.imputable = true)", folder).fetch()));
		invoicingFolder.setAnalyticMoveLineSet(new HashSet(Beans.get(AnalyticMoveLineRepository.class).all().filter("self.projectTask != null AND (self.projectTask.businessFolder = ?1 OR self.projectTask.project.businessFolder = ?1) AND (self.projectTask.saleOrder != null OR self.projectTask.project.saleOrder != null) AND self.invoiceable = true AND self.invoiced = false AND (self.projectTask.imputable = true OR self.projectTask.project.imputable = true)", folder).fetch()));
		invoicingFolder.setProjectTaskSet(new HashSet(Beans.get(ProjectTaskRepository.class).all().filter("(self.businessFolder = ?1 OR self.project.businessFolder = ?1) AND (self.saleOrder != null OR self.project.saleOrder != null) AND self.invoiceable = true AND self.invoiced = false AND self.imputable = true", folder).fetch()));


		response.setValues(invoicingFolder);
	}
}
