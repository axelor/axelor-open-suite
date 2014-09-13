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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.organisation.service.invoice.InvoiceGeneratorOrganisation;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;

public class PurchaseOrderInvoiceServiceAccountOrganisationImpl extends PurchaseOrderInvoiceServiceImpl {

	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderInvoiceServiceAccountOrganisationImpl.class);
	
	@Inject
	private InvoiceRepository invoiceRepo;
	

	public InvoiceGenerator createInvoiceGenerator(PurchaseOrder purchaseOrder) throws AxelorException  {

		if(purchaseOrder.getCurrency() == null)  {
			throw new AxelorException(String.format("Veuillez selectionner une devise pour la commande %s ", purchaseOrder.getPurchaseOrderSeq()), IException.CONFIGURATION_ERROR);
		}

		InvoiceGeneratorOrganisation invoiceGenerator = new InvoiceGeneratorOrganisation(invoiceRepo.OPERATION_TYPE_SUPPLIER_PURCHASE, purchaseOrder.getCompany(), purchaseOrder.getSupplierPartner(), 
				purchaseOrder.getContactPartner(), purchaseOrder.getProject(), purchaseOrder.getPriceList(), purchaseOrder.getPurchaseOrderSeq(), purchaseOrder.getExternalReference()) {

			@Override
			public Invoice generate() throws AxelorException {

				return super.createInvoiceHeader();
			}
		};

		return invoiceGenerator;
	}

	
}
