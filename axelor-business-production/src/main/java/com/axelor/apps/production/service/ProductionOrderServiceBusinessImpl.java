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

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.businessproject.db.BusinessFolder;
//import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProductionOrderServiceBusinessImpl extends ProductionOrderServiceImpl  {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private ManufOrderServiceBusinessImpl manufOrderService;
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void propagateIsToInvoice(ProductionOrder productionOrder) {

		logger.debug("{} is to invoice ? {}", productionOrder.getProductionOrderSeq(), productionOrder.getIsToInvoice());
		
		boolean isToInvoice = productionOrder.getIsToInvoice();
		
		if(productionOrder.getManufOrderList() != null)  {
			for(ManufOrder manufOrder : productionOrder.getManufOrderList())  {
				
				manufOrder.setIsToInvoice(isToInvoice);
				
				manufOrderService.propagateIsToInvoice(manufOrder);
			}
		}
		
		save(productionOrder);
		
	}

	public ProductionOrder createProductionOrder(BusinessFolder businessFolder, boolean isToInvoice) throws AxelorException  {
		
		ProductionOrder productionOrder = new ProductionOrder(this.getProductionOrderSeq());
		productionOrder.setBusinessFolder(businessFolder);
		productionOrder.setIsToInvoice(isToInvoice);
		
		return productionOrder;
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public ProductionOrder generateProductionOrder(Product product, BillOfMaterial billOfMaterial, BigDecimal qtyRequested, BusinessFolder businessFolder) throws AxelorException  {
		
		ProductionOrder productionOrder = this.createProductionOrder(businessFolder, false);
		
		this.addManufOrder(productionOrder, product, billOfMaterial, qtyRequested);
		
		return save(productionOrder);
		
	}
	
	
	
}
