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
package com.axelor.apps.stock.service;

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
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.supplychain.db.IMinStockRules;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.LocationLine;
import com.axelor.apps.supplychain.db.MinStockRules;
import com.axelor.apps.purchase.db.PurchaseOrder;
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
	public void generatePurchaseOrder(Product product, BigDecimal qty, LocationLine locationLine, Project project, int type) throws AxelorException  {
		
		Location location = locationLine.getLocation();
		
		//TODO à supprimer après suppression des variantes
		if(location == null)  {
			return;
		}
		
		MinStockRules minStockRules = this.getMinStockRules(product, location, type);
		
		if(minStockRules == null)  {
			return;
		}
		
		if(this.useMinStockRules(locationLine, minStockRules, qty, type))  {
			
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
							minStockRules.getName(),
							null, 
							supplychainConfigService.getSupplychainConfig(company).getSaleOrderInvoicingTypeSelect(), 
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
						
					purchaseOrderService.computePurchaseOrder(purchaseOrder);
					
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
	
	
	public boolean useMinStockRules(LocationLine locationLine, MinStockRules minStockRules, BigDecimal qty, int type)  {
		
		BigDecimal currentQty = locationLine.getCurrentQty();
		BigDecimal futureQty = locationLine.getFutureQty();
		
		BigDecimal minQty = minStockRules.getMinQty();
		
		if(type == IMinStockRules.TYPE_CURRENT)  {
			
			if(currentQty.compareTo(minQty) >= 0 && (currentQty.subtract(qty)).compareTo(minQty) == -1)  {
				return true;
			}
			
		}
		else  if(type == IMinStockRules.TYPE_FUTURE){
			
			if(futureQty.compareTo(minQty) >= 0 && (futureQty.subtract(qty)).compareTo(minQty) == -1)  {
				return true;
			}
			
		}
		return false;
		
	}
	
	public MinStockRules getMinStockRules(Product product, Location location, int type)  {
		
		return MinStockRules.filter("self.product = ?1 AND self.location = ?2 AND self.typeSelect = ?3", product, location, type).fetchOne();
		
		//TODO , plusieurs régles min de stock par produit (achat a 500 et production a 100)...
		
	}
	
	
}
