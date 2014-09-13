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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseServiceImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderPurchaseServiceAccountOrganisationImpl extends SaleOrderPurchaseServiceImpl  {

	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderPurchaseServiceImpl.class); 
	
	@Inject
	protected PurchaseOrderServiceAccountOrganisationImpl purchaseOrderServiceAccountOrganisationImpl;
	
	@Inject
	private PriceListRepository priceListRepo;
	
	@Inject
	private PurchaseOrderRepository purchaseOrderRepo;

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createPurchaseOrder(Partner supplierPartner, List<SaleOrderLine> saleOrderLineList, SaleOrder saleOrder) throws AxelorException  {
		
		LOG.debug("Création d'une commande fournisseur pour le devis client : {}",
				new Object[] { saleOrder.getSaleOrderSeq() });
		
		PurchaseOrder purchaseOrder = purchaseOrderServiceAccountOrganisationImpl.createPurchaseOrder(
				saleOrder.getProject(), 
				user, 
				saleOrder.getCompany(), 
				null, 
				supplierPartner.getCurrency(), 
				null, 
				saleOrder.getSaleOrderSeq(),
				saleOrder.getExternalReference(), 
				IPurchaseOrder.INVOICING_FREE, 
				purchaseOrderServiceAccountOrganisationImpl.getLocation(saleOrder.getCompany()), 
				today, 
				priceListRepo.all().filter("self.partner = ?1 AND self.typeSelect = 2", supplierPartner).fetchOne(), 
				supplierPartner);
		
		
		for(SaleOrderLine saleOrderLine : saleOrderLineList)  {
			
			purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLineServiceSupplychainImpl.createPurchaseOrderLine(purchaseOrder, saleOrderLine));
			
		}
		
		purchaseOrderServiceAccountOrganisationImpl.computePurchaseOrder(purchaseOrder);
		
		purchaseOrderRepo.save(purchaseOrder);
	}
}


