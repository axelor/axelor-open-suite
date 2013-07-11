package com.axelor.apps.accountorganisation.web

import com.axelor.apps.account.db.Invoice
import com.axelor.apps.accountorganisation.service.ProjectInvoiceService
import com.axelor.apps.organisation.db.Project
import com.axelor.apps.organisation.db.Task
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

class ProjectInvoiceController {

	@Inject
	private ProjectInvoiceService projectInvoiceService

	def createInvoice(ActionRequest request, ActionResponse response) {

		Project project = request.context as Project

		if(project) {

			try {
				Invoice invoice = projectInvoiceService.generateInvoice(project)

				if(invoice) {
					response.reload = true
					response.flash = "Facture créée"
				}
				else {
					response.flash = "Aucune facture générée. Veuillez vérifier que les champs Client, Contact et Société sont bien remplis."
				}
			}
			catch(Exception e)  { TraceBackService.trace(response, e) }
		}
	}
}
