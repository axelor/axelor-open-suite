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

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.supplychain.db.IMinStockRules;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.MinStockRules;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.service.config.SupplychainConfigService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MinStockRulesService {
	
	private static final Logger LOG = LoggerFactory.getLogger(MinStockRulesService.class); 

	@Inject
	private PurchaseOrderService purchaseOrderService;
	
	@Inject
	private PurchaseOrderLineService purchaseOrderLineService;
	
	@Inject
	private SupplychainConfigService supplychainConfigService;
	
	private LocalDate today;
	
	private UserInfo user;
	
	@Inject
	public MinStockRulesService(UserInfoService userInfoService) {

		this.today = GeneralService.getTodayDate();
		this.user = userInfoService.getUserInfo();
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void generatePurchaseOrder(Product product, BigDecimal qty, Location location, Project project, int type) throws AxelorException  {
		
		MinStockRules minStockRules = this.getMinStockRules(product, location, type);
		
		if(minStockRules != null && minStockRules.getMinQty().compareTo(qty) == 1)  {
			
			if(minStockRules.getOrderAlertSelect() == IMinStockRules.ORDER_ALERT_PURCHASE_ORDER)  {
				
				Partner supplierPartner = product.getDefaultSupplierPartner();
				
				if(supplierPartner != null)  {
					
					Company company = location.getCompany();
					
					PurchaseOrder purchaseOrder = purchaseOrderService.createPurchaseOrder(
							project, 
							this.user, 
							company, 
							null, 
							supplierPartner.getCurrency(), 
							this.today.plusDays(supplierPartner.getDeliveryDelay()), 
							null, 
							supplychainConfigService.getSupplychainConfig(company).getSalesOrderInvoicingTypeSelect(), 
							location, 
							this.today, 
							PriceList.filter("self.partner = ?1", supplierPartner).fetchOne(), 
							supplierPartner).save();
					
						
					purchaseOrder.addPurchaseOrderLineListItem(
							purchaseOrderLineService.createPurchaseOrderLine(
									purchaseOrder, 
									product, 
									"", 
									null, 
									minStockRules.getReOrderQty(), 
									product.getUnit(), 
									null));
						
					
					purchaseOrder.save();
					
				}
				
				
				
			}
			else if(minStockRules.getOrderAlertSelect() == IMinStockRules.ORDER_ALERT_PRODUCTION_ORDER)  {
				
				//TODO
			}
			else if(minStockRules.getOrderAlertSelect() == IMinStockRules.ORDER_ALERT_ALERT)  {
				
				//TODO
			}
			
			
			
			
		}
		
		
	}
	
	public MinStockRules getMinStockRules(Product product, Location location, int type)  {
		
		return MinStockRules.filter("self.product = ?1 AND self.location = ?2 AND self.typeSelect = ?3", product, location, type).fetchOne();
		
		//TODO , plusieurs régles min de stock par produit (achat a 500 et production a 100)...
		
	}
	
	
}
