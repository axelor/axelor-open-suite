package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class InvoiceController {

	@Inject
	private Provider<InvoiceService> is;

	@Inject
	private Provider<IrrecoverableService> ics;
	
	@Inject
	private Provider<JournalService> js;
	
	/**
	 * Fonction appeler par le bouton calculer Pied de facture
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public void computeFooter(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		try{
			is.get().computeFooter(invoice);

			response.setReload(true);
			response.setFlash("Montant de la facture : "+invoice.getInvoiceInTaxTotal()+" TTC");
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	/**
	 * Fonction appeler par le bouton calculer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void compute(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		try{
			is.get().compute(invoice);

			response.setReload(true);
			response.setFlash("Montant de la facture : "+invoice.getInvoiceInTaxTotal()+" TTC");
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	/**
	 * Fonction appeler par le bouton valider
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void validate(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		try{
			is.get().validate(invoice);
			response.setReload(true);
			response.setFlash("Facture "+invoice.getStatus().getName());
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	/**
	 * Fonction appeler par le bouton ventiler
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void ventilate(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		try {
			is.get().ventilate(invoice);
			response.setReload(true);
			response.setFlash("Facture "+invoice.getStatus().getName());
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	/**
	 * Passe l'état de la facture à "annulée"
	 * @param request
	 * @param response
	 * @throws AxelorException 
	 */
	public void cancel(ActionRequest request, ActionResponse response) throws AxelorException {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		is.get().cancel(invoice);
		response.setFlash("Facture "+invoice.getStatus().getName());
		response.setReload(true);
	}
	
	/**
	 * Fonction appeler par le bouton générer un avoir.
	 * 
	 * @param request
	 * @param response
	 */
	public void createRefund(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);

		try {
			is.get().createRefund(Invoice.find(invoice.getId()));
			response.setReload(true);
			response.setFlash("Avoir créé"); 
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	public void usherProcess(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		try {
			is.get().usherProcess(invoice);
		}
		catch (Exception e){
			TraceBackService.trace(response, e);
		}
	}
	
	public void passInIrrecoverable(ActionRequest request, ActionResponse response)  {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		try  {
			ics.get().passInIrrecoverable(invoice, true);
			response.setReload(true);
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	public void notPassInIrrecoverable(ActionRequest request, ActionResponse response)  {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		try  {
			ics.get().notPassInIrrecoverable(invoice);
			response.setReload(true);
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	public void getJournal(ActionRequest request, ActionResponse response)  {
		
		Invoice invoice = request.getContext().asType(Invoice.class);
//		invoice = Invoice.find(invoice.id)

		try  {
			response.setValue("journal", js.get().getJournal(invoice));
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	public void setOperationTypeSelect(ActionRequest request, ActionResponse response)  {
		Invoice invoice = request.getContext().asType(Invoice.class);
		response.setValue("operationTypeSelect", invoice.getOperationTypeSelect());
	}
}
