/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
import com.axelor.apps.accountorganisation.exceptions.IExceptionMessage;
import com.axelor.apps.accountorganisation.service.ProjectInvoiceService;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.db.repo.ProjectRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.axelor.i18n.I18n;

public class ProjectInvoiceController {

	@Inject
	private ProjectInvoiceService projectInvoiceService;
	
	public void createInvoice(ActionRequest request, ActionResponse response) {

		Project project = request.getContext().asType(Project.class);

		if(project != null) {

			try {
				Invoice invoice = projectInvoiceService.generateInvoice(Beans.get(ProjectRepository.class).find(project.getId()));

				if(invoice != null) {
					response.setReload(true);
					response.setFlash(I18n.get(com.axelor.apps.supplychain.exception.IExceptionMessage.PO_INVOICE_2));
				}
				else {
					response.setFlash(I18n.get(IExceptionMessage.PROJECT_INVOICE_1));
				}
			}
			catch(Exception e)  { TraceBackService.trace(response, e); }
		}
	}
}

