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
package com.axelor.apps.supplychain.service.invoice.generator;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;

public abstract class InvoiceGeneratorSupplyChain extends InvoiceGenerator {

	protected SaleOrder saleOrder;

	protected InvoiceGeneratorSupplyChain(int operationType, Company company,PaymentCondition paymentCondition, PaymentMode paymentMode, Address mainInvoicingAddress,
			Partner partner, Partner contactPartner, Currency currency, PriceList priceList, String internalReference, String externalReference, SaleOrder saleOrder) throws AxelorException {

		super(operationType, company, paymentCondition, paymentMode, mainInvoicingAddress, partner, contactPartner, currency, priceList, internalReference, externalReference);
		this.saleOrder = saleOrder;

	}


	/**
	 * PaymentCondition, Paymentmode, MainInvoicingAddress, Currency récupérés du tiers
	 * @param operationType
	 * @param company
	 * @param partner
	 * @param contactPartner
	 * @throws AxelorException
	 */
	protected InvoiceGeneratorSupplyChain(int operationType, Company company, Partner partner, Partner contactPartner, PriceList priceList,
			String internalReference, String externalReference, SaleOrder saleOrder) throws AxelorException {

		super(operationType, company, partner, contactPartner, priceList, internalReference, externalReference);
		this.saleOrder = saleOrder;

	}


	protected Invoice createInvoiceHeader() throws AxelorException  {

		Invoice invoice = super.createInvoiceHeader();

		if (!GeneralService.getGeneral().getManageAmountInvoiceByLine()){
			invoice.setSaleOrder(saleOrder);
		}

		return invoice;
	}

}
