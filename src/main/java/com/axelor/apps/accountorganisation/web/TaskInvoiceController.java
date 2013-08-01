package com.axelor.apps.accountorganisation.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.accountorganisation.service.TaskInvoiceService;
import com.axelor.apps.organisation.db.Task;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class TaskInvoiceController {

	@Inject
	private TaskInvoiceService taskInvoiceService;
	
	public void createInvoice(ActionRequest request, ActionResponse response) {
		
		Task task = request.getContext().asType(Task.class);
		
		try {
			task = Task.find(task.getId());
			
			Invoice invoice = taskInvoiceService.generateInvoice(task);
			
			if(invoice != null)  {
				response.setReload(true);
				response.setFlash("Facture créée");
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
