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
package com.axelor.apps.suppliermanagement.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.SupplierCatalog;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderLineService;
import com.axelor.apps.supplychain.service.PurchaseOrderService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PurchaseOrderSupplierService {
	
	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderSupplierService.class);
	
	@Inject
	private PurchaseOrderSupplierLineService purchaseOrderSupplierLineService;
	
	@Inject
	private PurchaseOrderService purchaseOrderService;
	
	@Inject
	private PurchaseOrderLineService purchaseOrderLineService;
	
	private LocalDate today;
	
	private UserInfo user;
	
	@Inject
	public PurchaseOrderSupplierService(UserInfoService userInfoService) {

		this.today = GeneralService.getTodayDate();
		this.user = userInfoService.getUserInfo();
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void generateSuppliersRequests(PurchaseOrderLine purchaseOrderLine)  {
		
		Product product = purchaseOrderLine.getProduct();
		
		if(product != null && product.getSupplierCatalogList() != null)  {
			
			for(SupplierCatalog supplierCatalog : product.getSupplierCatalogList())  {
				
				purchaseOrderLine.addPurchaseOrderSupplierLineListItem(purchaseOrderSupplierLineService.create(supplierCatalog.getSupplierPartner()));
				
			}
		}
		
		purchaseOrderLine.save();
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void generateSuppliersPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException  {
		
		if(purchaseOrder.getPurchaseOrderLineList() == null)  {  return;  }
		
		Map<Partner,List<PurchaseOrderLine>> purchaseOrderLinesBySupplierPartner = this.splitBySupplierPartner(purchaseOrder.getPurchaseOrderLineList());
		
		for(Partner supplierPartner : purchaseOrderLinesBySupplierPartner.keySet())  {
			
			this.createPurchaseOrder(supplierPartner, purchaseOrderLinesBySupplierPartner.get(supplierPartner), purchaseOrder);
			
		}
		

		purchaseOrder.save();
	
	}
	
	
	public Map<Partner,List<PurchaseOrderLine>> splitBySupplierPartner(List<PurchaseOrderLine> purchaseOrderLineList) throws AxelorException  {
		
		Map<Partner,List<PurchaseOrderLine>> purchaseOrderLinesBySupplierPartner = new HashMap<Partner,List<PurchaseOrderLine>>();
		
		for(PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList)  {
			
			Partner supplierPartner = purchaseOrderLine.getSupplierPartner();
			
			if(supplierPartner == null)  {
				
				throw new AxelorException(String.format("Veuillez choisir un fournisseur pour la ligne %s", purchaseOrderLine.getProductName()), IException.CONFIGURATION_ERROR);
			}
			
			if(!purchaseOrderLinesBySupplierPartner.containsKey(supplierPartner))  {
				purchaseOrderLinesBySupplierPartner.put(supplierPartner, new ArrayList<PurchaseOrderLine>());
			}
			
			purchaseOrderLinesBySupplierPartner.get(supplierPartner).add(purchaseOrderLine);
			
		}
		
		return purchaseOrderLinesBySupplierPartner;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createPurchaseOrder(Partner supplierPartner, List<PurchaseOrderLine> purchaseOrderLineList, PurchaseOrder parentPurchaseOrder) throws AxelorException  {
		
		LOG.debug("Création d'une commande fournisseur depuis le devis fournisseur : {} et le fournisseur : {}", 
				new Object[] { parentPurchaseOrder.getPurchaseOrderSeq(), supplierPartner.getFullName() });
		
		PurchaseOrder purchaseOrder = purchaseOrderService.createPurchaseOrder(
				parentPurchaseOrder.getProject(), 
				user, 
				parentPurchaseOrder.getCompany(), 
				null, 
				supplierPartner.getCurrency(), 
				null, 
				parentPurchaseOrder.getPurchaseOrderSeq(),
				parentPurchaseOrder.getExternalReference(), 
				parentPurchaseOrder.getInvoicingTypeSelect(), 
				purchaseOrderService.getLocation(parentPurchaseOrder.getCompany()), 
				today, 
				PriceList.all().filter("self.partner = ?1 AND self.typeSelect = 2", supplierPartner).fetchOne(), 
				supplierPartner);
		
		purchaseOrder.setParentPurchaseOrder(parentPurchaseOrder);
		
		
		for(PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList)  {
			
			purchaseOrder.addPurchaseOrderLineListItem(this.createPurchaseOrderLine(purchaseOrder, purchaseOrderLine));
			
		}
		
		purchaseOrderService.computePurchaseOrder(purchaseOrder);
		
		purchaseOrder.save();
	}
	
	
	public PurchaseOrderLine createPurchaseOrderLine(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException  {

		LOG.debug("Création d'une ligne de commande fournisseur pour le produit : {}",
				new Object[] { purchaseOrderLine.getProductName() });
		
		return purchaseOrderLineService.createPurchaseOrderLine(
				purchaseOrder, 
				purchaseOrderLine.getProduct(), 
				purchaseOrderLine.getDescription(), 
				null,
				purchaseOrderLine.getQty(), 
				purchaseOrderLine.getUnit(), 
				purchaseOrderLine.getTask());
		
	}
}
