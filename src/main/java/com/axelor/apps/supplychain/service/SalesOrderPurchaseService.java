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
					
					throw new AxelorException(String.format("Veuillez choisir un fournisseur pour la ligne %s", salesOrderLine.getProductName()), IException.CONFIGURATION_ERROR);
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
				salesOrder.getSalesOrderSeq(),
				salesOrder.getExternalReference(), 
				IPurchaseOrder.INVOICING_FREE, 
				purchaseOrderService.getLocation(salesOrder.getCompany()), 
				today, 
				PriceList.filter("self.partner = ?1 AND self.typeSelect = 2", supplierPartner).fetchOne(), 
				supplierPartner);
		
		
		for(SalesOrderLine salesOrderLine : salesOrderLineList)  {
			
			purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLineService.createPurchaseOrderLine(purchaseOrder, salesOrderLine));
			
		}
		
		purchaseOrderService.computePurchaseOrder(purchaseOrder);
		
		purchaseOrder.save();
	}
}


