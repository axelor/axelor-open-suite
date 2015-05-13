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
package com.axelor.apps.supplychain.service;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.Location;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;

public class SaleOrderServiceSupplychainImpl extends SaleOrderServiceStockImpl {

	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderServiceSupplychainImpl.class);

	public SaleOrder createSaleOrder(User buyerUser, Company company, Partner contactPartner, Currency currency,
			LocalDate deliveryDate, String internalReference, String externalReference, int invoicingTypeSelect, Location location, LocalDate orderDate,
			PriceList priceList, Partner clientPartner) throws AxelorException  {

		LOG.debug("Création d'une commande fournisseur : Société = {},  Reference externe = {}, Client = {}",
				new Object[] { company.getName(), externalReference, clientPartner.getFullName() });

		SaleOrder saleOrder = super.createSaleOrder(buyerUser, company, contactPartner, currency, deliveryDate, internalReference,
				externalReference, orderDate, priceList, clientPartner);

		saleOrder.setLocation(location);
		saleOrder.setInvoicingTypeSelect(invoicingTypeSelect);

		return saleOrder;
	}

}



