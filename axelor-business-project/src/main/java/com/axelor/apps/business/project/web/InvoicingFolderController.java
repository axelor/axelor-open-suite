package com.axelor.apps.business.project.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.business.project.exception.IExceptionMessage;
import com.axelor.apps.business.project.service.InvoicingFolderService;
import com.axelor.apps.businessproject.db.InvoicingFolder;
import com.axelor.apps.businessproject.db.repo.InvoicingFolderRepository;
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
				&& folder.getLogTimesSet().isEmpty() && folder.getExpenseLineSet().isEmpty() && folder.getAnalyticMoveLineSet().isEmpty()){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_FOLDER_EMPTY)), IException.CONFIGURATION_ERROR);
		}
		if(folder.getFolder() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_FOLDER_FOLDER)), IException.CONFIGURATION_ERROR);
		}
		if(folder.getFolder().getCustomer() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_FOLDER_CUSTOMER)), IException.CONFIGURATION_ERROR);
		}

		if(folder.getFolder().getUserResponsible() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICING_FOLDER_USER)), IException.CONFIGURATION_ERROR);
		}
		Invoice invoice = invoicingFolderService.generateInvoice(folder);
		response.setView(ActionView
				.define("Invoice")
				.model(Invoice.class.getName())
				.add("form", "invoice-form")
				.param("forceEdit", "true")
				.context("_showRecord", String.valueOf(invoice.getId())).map());
	}
}
