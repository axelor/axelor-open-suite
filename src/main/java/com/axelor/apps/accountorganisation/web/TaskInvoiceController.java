/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.accountorganisation.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.accountorganisation.service.TaskInvoiceService;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.db.repo.TaskRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class TaskInvoiceController {

	@Inject
	private TaskInvoiceService taskInvoiceService;
	
	@Inject
	private TaskRepository taskRepo;
	
	public void createInvoice(ActionRequest request, ActionResponse response) {
		
		Task task = request.getContext().asType(Task.class);
		
		try {
			task = taskRepo.find(task.getId());
			
			Invoice invoice = taskInvoiceService.generateInvoice(task);
			
			if(invoice != null)  {
				response.setReload(true);
				response.setFlash("Facture créée");
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
