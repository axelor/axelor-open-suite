/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.supplychain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.supplychain.db.IPurchaseOrder;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SalesOrderPurchaseService {

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderPurchaseService.class); 

	@Inject
	private PurchaseOrderService purchaseOrderService;
	
	@Inject
	private PurchaseOrderLineService purchaseOrderLineService;

	private LocalDate today;
	
	private UserInfo user;
	
	@Inject
	public SalesOrderPurchaseService(UserInfoService userInfoService) {

		this.today = GeneralService.getTodayDate();
		this.user = userInfoService.getUserInfo();
	}
	

	public void createPurchaseOrders(SalesOrder salesOrder) throws AxelorException  {
		
		Map<Partner,List<SalesOrderLine>> salesOrderLinesBySupplierPartner = this.splitBySupplierPartner(salesOrder.getSalesOrderLineList());
		
		for(Partner supplierPartner : salesOrderLinesBySupplierPartner.keySet())  {
			
			this.createPurchaseOrder(supplierPartner, salesOrderLinesBySupplierPartner.get(supplierPartner), salesOrder);
			
		}
		
	}
	
	
	public Map<Partner,List<SalesOrderLine>> splitBySupplierPartner(List<SalesOrderLine> salesOrderLineList) throws AxelorException  {
		
		Map<Partner,List<SalesOrderLine>> salesOrderLinesBySupplierPartner = new HashMap<Partner,List<SalesOrderLine>>();
		
		for(SalesOrderLine salesOrderLine : salesOrderLineList)  {
			
			if(salesOrderLine.getSaleSupplySelect() == IProduct.SALE_SUPPLY_PURCHASE)  {
			
				Partner supplierPartner = salesOrderLine.getSupplierPartner();
				
				if(supplierPartner == null)  {
					
					throw new AxelorException(String.format("Veuillez choisir un fournisseur pour la ligne {}", salesOrderLine.getProductName()), IException.CONFIGURATION_ERROR);
				}
				
				if(!salesOrderLinesBySupplierPartner.containsKey(supplierPartner))  {
					salesOrderLinesBySupplierPartner.put(supplierPartner, new ArrayList<SalesOrderLine>());
				}
				
				salesOrderLinesBySupplierPartner.get(supplierPartner).add(salesOrderLine);
			}
			
		}
		
		return salesOrderLinesBySupplierPartner;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createPurchaseOrder(Partner supplierPartner, List<SalesOrderLine> salesOrderLineList, SalesOrder salesOrder) throws AxelorException  {
		
		LOG.debug("Création d'une commande fournisseur pour le devis client : {}",
				new Object[] { salesOrder.getSalesOrderSeq() });
		
		PurchaseOrder purchaseOrder = purchaseOrderService.createPurchaseOrder(
				salesOrder.getProject(), 
				user, 
				salesOrder.getCompany(), 
				null, 
				supplierPartner.getCurrency(), 
				null, 
				null, 
				IPurchaseOrder.INVOICING_FREE, 
				purchaseOrderService.getLocation(salesOrder.getCompany()), 
				today, 
				PriceList.all().filter("self.partner = ?1 AND self.typeSelect = 2", supplierPartner).fetchOne(), 
				supplierPartner);
		
		
		for(SalesOrderLine salesOrderLine : salesOrderLineList)  {
			
			purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLineService.createPurchaseOrderLine(purchaseOrder, salesOrderLine));
			
		}
		
		purchaseOrderService.computePurchaseOrder(purchaseOrder);
		
		purchaseOrder.save();
	}
}


