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
package com.axelor.apps.accountorganisation.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.service.invoice.InvoiceGeneratorOrganisation;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectInvoiceService {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectInvoiceService.class);

	@Inject
	private TaskInvoiceService taskInvoiceService;


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateInvoice(Project project) throws AxelorException {

		Invoice invoice = this.createInvoice(project);
		invoice.save();
		return invoice;
	}

	public Invoice createInvoice(Project project) throws AxelorException {

		
		InvoiceGeneratorOrganisation invoiceGenerator = new InvoiceGeneratorOrganisation(Invoice.OPERATION_TYPE_CLIENT_SALE, project.getCompany(), project.getClientPartner(), 
				project.getContactPartner(), project, null, project.getName(), null) {

			@Override
			public Invoice generate() throws AxelorException {

				return super.createInvoiceHeader();
			}
		};

		Invoice invoice = invoiceGenerator.generate();
		invoiceGenerator.populate(invoice, this.createInvoiceLines(invoice, project));
		return invoice;
	}
	
	
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, Project project) throws AxelorException  {
		
		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		
		for(Task task : project.getTaskList()) {
			
			invoiceLineList.addAll(taskInvoiceService.createInvoiceLines(invoice, task));
				
		}
		return invoiceLineList;	
	}
	
}
