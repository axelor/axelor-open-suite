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

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.stock.db.IMinStockRules;
import com.axelor.apps.stock.service.MinStockRulesServiceImpl;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.MinStockRules;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MinStockRulesServiceSupplychainImpl extends MinStockRulesServiceImpl  {
	
	private static final Logger LOG = LoggerFactory.getLogger(MinStockRulesServiceImpl.class); 

	@Inject
	protected PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychainImpl;
	
	@Inject
	protected PurchaseOrderLineService purchaseOrderLineService;
	
	@Inject
	protected SaleConfigService saleConfigService;
	
	protected LocalDate today;
	
	protected UserInfo user;
	
	@Inject
	public MinStockRulesServiceSupplychainImpl(UserInfoService userInfoService) {

		super(userInfoService);
	}
	
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void generatePurchaseOrder(Product product, BigDecimal qty, LocationLine locationLine, int type) throws AxelorException  {
		
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
			
			if(minStockRules.getOrderAlertSelect() == IMinStockRules.ORDER_ALERT_ALERT)  {
				
				//TODO
				
			}
			else if(minStockRules.getOrderAlertSelect() == IMinStockRules.ORDER_ALERT_PRODUCTION_ORDER)  {
				
				
			}
			else if(minStockRules.getOrderAlertSelect() == IMinStockRules.ORDER_ALERT_PURCHASE_ORDER)  {
				
				Partner supplierPartner = product.getDefaultSupplierPartner();
				
				if(supplierPartner != null)  {
					
					Company company = location.getCompany();
					
					PurchaseOrder purchaseOrder = purchaseOrderServiceSupplychainImpl.createPurchaseOrder(
							this.user, 
							company, 
							null, 
							supplierPartner.getCurrency(), 
							this.today.plusDays(supplierPartner.getDeliveryDelay()), 
							minStockRules.getName(),
							null, 
							saleConfigService.getSaleConfig(company).getSaleOrderInvoicingTypeSelect(), 
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
									product.getUnit()));
						
					purchaseOrderServiceSupplychainImpl.computePurchaseOrder(purchaseOrder);
					
					purchaseOrder.save();
					
				}
				
				
			}
			
			
			
			
		}
		
	}
	
	
}
