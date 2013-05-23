package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.Invoice
import com.axelor.apps.account.service.IrrecoverableService
import com.axelor.apps.account.service.JournalService
import com.axelor.apps.account.service.invoice.InvoiceService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject
import com.google.inject.Provider

@Slf4j
class InvoiceController {

	@Inject
	private Provider<InvoiceService> is

	@Inject
	private Provider<IrrecoverableService> ics
	
	@Inject
	private Provider<JournalService> js
	
	/**
	 * Fonction appeler par le bouton calculer Pied de facture
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	def void computeFooter(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.context as Invoice
		invoice = Invoice.find(invoice.id)

		try{

			is.get().computeFooter(invoice)

			response.reload = true
			response.flash = "Montant de la facture : ${invoice.invoiceInTaxTotal} TTC"
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e)
		}
	}


	/**
	 * Fonction appeler par le bouton calculer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	def void compute(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.context as Invoice
		invoice = Invoice.find(invoice.id)

		try{

			is.get().compute(invoice)

			response.reload = true
			response.flash = "Montant de la facture : ${invoice.invoiceInTaxTotal} TTC"
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e)
		}
	}


	/**
	 * Fonction appeler par le bouton valider
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	def void validate(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.context as Invoice
		invoice = Invoice.find(invoice.id)

		try{

			is.get().validate(invoice)
			response.reload = true
			response.flash = "Facture ${invoice.status.name}"
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e)
		}
	}

	/**
	 * Fonction appeler par le bouton ventiler
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	def void ventilate(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.context as Invoice
		invoice = Invoice.find(invoice.id)

		try {

			is.get().ventilate(invoice)
			response.reload = true
			response.flash = "Facture ${invoice.status.name}"
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e)
		}
	}

	/**
	 * Passe l'état de la facture à "annulée"
	 * @param request
	 * @param response
	 */
	def void cancel(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.context as Invoice
		invoice = Invoice.find(invoice.id)

		is.get().cancel(invoice)
		response.flash = "Facture ${invoice.status.name}"

		response.reload = true
	}


	/**
	 * Fonction appeler par le bouton générer un avoir.
	 * 
	 * @param request
	 * @param response
	 */
	def void createRefund(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.context as Invoice

		try {

			is.get().createRefund(Invoice.find(invoice.id))
			response.reload = true
			response.flash = "Avoir créé"
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e)
		}
	}


	def void usherProcess(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.context as Invoice
		invoice = Invoice.find(invoice.id)

		try {

			is.get().usherProcess(invoice)
		}
		catch (Exception e){
			TraceBackService.trace(response, e)
		}
	}


	def void passInIrrecoverable(ActionRequest request, ActionResponse response)  {

		Invoice invoice = request.context as Invoice
		invoice = Invoice.find(invoice.id)

		try  {

			ics.get().passInIrrecoverable(invoice, true)
			response.reload = true
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e)
		}
	}

	def void notPassInIrrecoverable(ActionRequest request, ActionResponse response)  {

		Invoice invoice = request.context as Invoice
		invoice = Invoice.find(invoice.id)

		try  {

			ics.get().notPassInIrrecoverable(invoice)
			response.reload = true
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e)
		}
	}
	
	
	def void getJournal(ActionRequest request, ActionResponse response)  {
		
		Invoice invoice = request.context as Invoice
//		invoice = Invoice.find(invoice.id)

		try  {

			response.values = ["journal" : js.get().getJournal(invoice)]

		}
		catch(Exception e)  {
			TraceBackService.trace(response, e)
		}
		
	}
}

