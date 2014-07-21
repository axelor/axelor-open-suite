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
package com.axelor.apps.organisation.service.invoice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.organisation.db.Project;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public abstract class InvoiceGeneratorOrganisation extends InvoiceGenerator  {
	
	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(InvoiceGeneratorOrganisation.class);

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
	
	protected int inverseOperationType(int operationType) throws AxelorException  {

		switch(operationType)  {
		
			case IInvoice.SUPPLIER_PURCHASE:
				return IInvoice.SUPPLIER_REFUND;
			case IInvoice.SUPPLIER_REFUND:
				return IInvoice.SUPPLIER_PURCHASE;
			case IInvoice.CLIENT_SALE:
				return IInvoice.CLIENT_REFUND;
			case IInvoice.CLIENT_REFUND:
				return IInvoice.CLIENT_SALE;
			default:
				throw new AxelorException(String.format("%s :\nLe type de facture n'est pas rempli %s", GeneralServiceAccount.getExceptionInvoiceMsg()), IException.MISSING_FIELD);	
		}
		
	}
	
	
	abstract public Invoice generate() throws AxelorException;
	
	
	@Override
	protected Invoice createInvoiceHeader() throws AxelorException  {
		
		Invoice invoice = super.createInvoiceHeader();
		
		invoice.setProject(project);
		
		return invoice;
	}
	

}
