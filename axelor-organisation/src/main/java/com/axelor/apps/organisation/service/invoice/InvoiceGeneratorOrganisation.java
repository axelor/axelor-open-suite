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
package com.axelor.apps.organisation.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.organisation.db.Project;
import com.axelor.exception.AxelorException;

public class InvoiceGeneratorOrganisation extends InvoiceGenerator  {
	
	protected Project project;
	
	
	protected InvoiceGeneratorOrganisation(int operationType, Company company,PaymentCondition paymentCondition, PaymentMode paymentMode, Address mainInvoicingAddress, 
			Partner partner, Partner contactPartner, Currency currency, Project project, PriceList priceList, String internalReference, String externalReference) throws AxelorException {
		
		super(operationType, company, paymentCondition, paymentMode, mainInvoicingAddress, 
				partner, contactPartner, currency, priceList, internalReference, externalReference);
				
		this.project = project;
		
	}
	
	
	/**
	 * PaymentCondition, Paymentmode, MainInvoicingAddress, Currency récupérés du tiers
	 * @param operationType
	 * @param company
	 * @param partner
	 * @param contactPartner
	 * @throws AxelorException
	 */
	protected InvoiceGeneratorOrganisation(int operationType, Company company, Partner partner, Partner contactPartner, Project project, PriceList priceList, 
			String internalReference, String externalReference) throws AxelorException {
		
		super(operationType, company, partner, contactPartner, priceList, internalReference, externalReference);
		
		this.project = project;
		
	}
	
	
	protected InvoiceGeneratorOrganisation() {

		super();
		
	}
	
	
	
	public Invoice generate() throws AxelorException {
		return null;
	}
	
	
	@Override
	protected Invoice createInvoiceHeader() throws AxelorException  {
		
		Invoice invoice = super.createInvoiceHeader();
		
		invoice.setProject(project);
		
		return invoice;
	}
	

}
