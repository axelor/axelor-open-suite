package com.axelor.apps.accountorganisation.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.accountorganisation.service.ProjectInvoiceService;
import com.axelor.apps.organisation.db.Project;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProjectInvoiceController {

	@Inject
	private ProjectInvoiceService projectInvoiceService;
	
	public void createInvoice(ActionRequest request, ActionResponse response) {

		Project project = request.getContext().asType(Project.class);

		if(project != null) {

			try {
				Invoice invoice = projectInvoiceService.generateInvoice(project);

				if(invoice != null) {
					response.setReload(true);
					response.setFlash("Facture créée");
				}
				else {
					response.setFlash("Aucune facture générée. Veuillez vérifier que les champs Client, Contact et Société sont bien remplis.");
				}
			}
			catch(Exception e)  { TraceBackService.trace(response, e); }
		}
	}
}
