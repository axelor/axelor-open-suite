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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.auth.db.User;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.stock.db.IMinStockRules;
import com.axelor.apps.stock.service.MinStockRulesServiceImpl;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.MinStockRules;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MinStockRulesServiceSupplychainImpl extends MinStockRulesServiceImpl  {
	
	@Inject
	protected PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychainImpl;
	
	@Inject
	protected PurchaseOrderLineService purchaseOrderLineService;
	
	@Inject
	protected SaleConfigService saleConfigService;
	
	protected LocalDate today;
	
	protected User user;
	
	@Inject
	private PurchaseOrderRepository purchaseOrderRepo;
	
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
					
					PurchaseOrder purchaseOrder = purchaseOrderRepo.save(purchaseOrderServiceSupplychainImpl.createPurchaseOrder(
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
							Beans.get(PriceListRepository.class).all().filter("self.partner = ?1", supplierPartner).fetchOne(), 
							supplierPartner));
					
					purchaseOrder.addPurchaseOrderLineListItem(
							purchaseOrderLineService.createPurchaseOrderLine(
									purchaseOrder, 
									product, 
									"", 
									null, 
									minStockRules.getReOrderQty(), 
									product.getUnit()));
						
					purchaseOrderServiceSupplychainImpl.computePurchaseOrder(purchaseOrder);
					
					purchaseOrderRepo.save(purchaseOrder);
					
				}
				
				
			}
			
			
			
			
		}
		
	}
	
	
}
