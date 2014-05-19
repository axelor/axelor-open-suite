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
package com.axelor.apps.production.service;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.supplychain.db.IPurchaseOrder;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.service.SalesOrderService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProductionOrderSalesOrderService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private ProductionOrderService productionOrderService;
	
	@Inject
	private SalesOrderService salesOrderService;
	
	private LocalDate today;
	
	private UserInfo user;
	
	@Inject
	public ProductionOrderSalesOrderService(UserInfoService userInfoService) {

		this.today = GeneralService.getTodayDate();
		this.user = userInfoService.getUserInfo();
	}
	
	
	public void generateProductionOrder(SalesOrder salesOrder) throws AxelorException  {
		
		if(salesOrder.getSalesOrderLineList() != null)  {
			
			for(SalesOrderLine salesOrderLine : salesOrder.getSalesOrderLineList())  {
				
				this.generateProductionOrder(salesOrderLine);
				
			}
			
		}
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public ProductionOrder generateProductionOrder(SalesOrderLine salesOrderLine) throws AxelorException  {
		
		Product product = salesOrderLine.getProduct();
		
		if(salesOrderLine.getSaleSupplySelect() == IProduct.SALE_SUPPLY_PRODUCE && product != null && product.getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_STORABLE) )  {
			
			BillOfMaterial billOfMaterial = salesOrderLine.getBillOfMaterial();
			
			if(billOfMaterial == null)  {
				
				billOfMaterial = product.getDefaultBillOfMaterial();
				
			}
			
			if(billOfMaterial == null && product.getParentProduct() != null)  {
				
				billOfMaterial = product.getParentProduct().getDefaultBillOfMaterial();
				
			}
			
			if(billOfMaterial == null)  {
				
				throw new AxelorException(
						String.format(JPA.translate(IExceptionMessage.PRODUCTION_ORDER_SALES_ORDER_NO_BOM), product.getName(), product.getCode()), 
						IException.CONFIGURATION_ERROR);
				
			}
			
			return productionOrderService.generateProductionOrder(product, billOfMaterial, salesOrderLine.getQty(), salesOrderLine.getSalesOrder().getProject()).save();
		
		}
		
		return null;
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createSalesOrder(ProductionOrder productionOrder) throws AxelorException  {
		
		logger.debug("Création d'un devis client pour l'ordre de production : {}",
				new Object[] { productionOrder.getProductionOrderSeq() });
		
		Project businessProject = productionOrder.getBusinessProject();
		
		Partner partner = businessProject.getClientPartner();
		
		if(businessProject.getCompany() != null)  {
		
			SalesOrder salesOrder = salesOrderService.createSalesOrder(
					businessProject, 
					user, 
					businessProject.getCompany(), 
					null, 
					partner.getCurrency(), 
					null, 
					null,
					null, 
					IPurchaseOrder.INVOICING_FREE, 
					salesOrderService.getLocation(businessProject.getCompany()), 
					today, 
					PriceList.filter("self.partner = ?1 AND self.typeSelect = 1", partner).fetchOne(), 
					partner);
			
			salesOrder.save();
			
		}
		
		//TODO 
		
//		for(SalesOrderLine salesOrderLine : salesOrderLineList)  {
//			
//			purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLineService.createPurchaseOrderLine(purchaseOrder, salesOrderLine));
//			
//		}
//		
//		purchaseOrderService.computePurchaseOrder(purchaseOrder);
//		
//		purchaseOrder.save();
	}
	
	
}
