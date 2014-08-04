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

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.exception.AxelorException;

public class PurchaseOrderServiceAccountOrganisationImpl extends PurchaseOrderServiceSupplychainImpl {

	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderServiceAccountOrganisationImpl.class); 

	public PurchaseOrder createPurchaseOrder(Project project, UserInfo buyerUserInfo, Company company, Partner contactPartner, Currency currency, 
			LocalDate deliveryDate, String internalReference, String externalReference, int invoicingTypeSelect, Location location, LocalDate orderDate, 
			PriceList priceList, Partner supplierPartner) throws AxelorException  {
		
		LOG.debug("Création d'une commande fournisseur : Société = {},  Reference externe = {}, Fournisseur = {}",
				new Object[] { company.getName(), externalReference, supplierPartner.getFullName() });
		
		PurchaseOrder purchaseOrder = super.createPurchaseOrder(buyerUserInfo, company, contactPartner, currency, deliveryDate, 
				internalReference, externalReference, invoicingTypeSelect, orderDate, priceList, supplierPartner);
				
		purchaseOrder.setProject(project);
		
		return purchaseOrder;
	}
	
	
	
	
}
