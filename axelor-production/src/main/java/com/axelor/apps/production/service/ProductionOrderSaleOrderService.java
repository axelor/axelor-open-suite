/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserService;
//import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProductionOrderSaleOrderService{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private LocalDate today;
	
	private User user;
	
	@Inject
	public ProductionOrderSaleOrderService(UserService userInfoService) {

		this.today = GeneralService.getTodayDate();
		this.user = userInfoService.getUser();
	}
	
	
	public void generateProductionOrder(SaleOrder saleOrder) throws AxelorException  {
		
		if(saleOrder.getSaleOrderLineList() != null)  {
			
			for(SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList())  {
				
				this.generateProductionOrder(saleOrderLine);
				
			}
			
		}
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public ProductionOrder generateProductionOrder(SaleOrderLine saleOrderLine) throws AxelorException  {
		
		Product product = saleOrderLine.getProduct();
		
		if(saleOrderLine.getSaleSupplySelect() == IProduct.SALE_SUPPLY_PRODUCE && product != null && product.getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_STORABLE) )  {
			
			BillOfMaterial billOfMaterial = saleOrderLine.getBillOfMaterial();
			
			if(billOfMaterial == null)  {
				
				billOfMaterial = product.getDefaultBillOfMaterial();
				
			}
			
			if(billOfMaterial == null && product.getParentProduct() != null)  {
				
				billOfMaterial = product.getParentProduct().getDefaultBillOfMaterial();
				
			}
			
			if(billOfMaterial == null)  {
				
				throw new AxelorException(
						String.format(I18n.get(IExceptionMessage.PRODUCTION_ORDER_SALES_ORDER_NO_BOM), product.getName(), product.getCode()), 
						IException.CONFIGURATION_ERROR);
				
			}
			
//			return productionOrderService.generateProductionOrder(product, billOfMaterial, saleOrderLine.getQty(), saleOrderLine.getSaleOrder().getProject()).save();
		
		}
		
		return null;
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createSaleOrder(ProductionOrder productionOrder) throws AxelorException  {
		
		logger.debug("Création d'un devis client pour l'ordre de production : {}",
				new Object[] { productionOrder.getProductionOrderSeq() });
		
//		Project businessProject = productionOrder.getBusinessProject();
		
//		Partner partner = businessProject.getClientPartner();
//		
//		if(businessProject.getCompany() != null)  {
//		
//			SaleOrder saleOrder = saleOrderServiceStockImpl.createSaleOrder(
//					businessProject, 
//					user, 
//					businessProject.getCompany(), 
//					null, 
//					partner.getCurrency(), 
//					null, 
//					null,
//					null, 
//					IPurchaseOrder.INVOICING_FREE, 
//					saleOrderServiceStockImpl.getLocation(businessProject.getCompany()), 
//					today, 
//					PriceList.filter("self.partner = ?1 AND self.typeSelect = 1", partner).fetchOne(), 
//					partner);
//			
//			saleOrder.save();
			
//		}
		
		//TODO 
		
//		for(SaleOrderLine saleOrderLine : saleOrderLineList)  {
//			
//			purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLineService.createPurchaseOrderLine(purchaseOrder, saleOrderLine));
//			
//		}
//		
//		purchaseOrderService.computePurchaseOrder(purchaseOrder);
//		
//		purchaseOrder.save();
	}
	
	
}
