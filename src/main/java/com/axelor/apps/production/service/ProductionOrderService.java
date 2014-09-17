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
package com.axelor.apps.production.service;

import java.math.BigDecimal;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.administration.SequenceService;
//import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProductionOrderService extends ProductionOrderRepository{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private ManufOrderService manufOrderService;
	
	@Inject
	private SequenceService sequenceService;
	
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

	
//	public ProductionOrder createProductionOrder(Project businessProject, boolean isToInvoice) throws AxelorException  {
//		
//		return new ProductionOrder(
//				this.getProductionOrderSeq(), 
//				isToInvoice, 
//				businessProject);
//		
//		
//	}
	
	
	public String getProductionOrderSeq() throws AxelorException  {
		
		String seq = sequenceService.getSequenceNumber(IAdministration.PRODUCTION_ORDER);
		
		if(seq == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.PRODUCTION_ORDER_SEQ), IException.CONFIGURATION_ERROR);
		}
		
		return seq;
	}
	
	
	/**
	 * Generate a Production Order
	 * @param product
	 * 		Product must be passed in param because product can be different of bill of material product (Product variant)
	 * @param billOfMaterial
	 * @param qtyRequested
	 * @param businessProject
	 * @return
	 * @throws AxelorException
	 */
//	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
//	public ProductionOrder generateProductionOrder(Product product, BillOfMaterial billOfMaterial, BigDecimal qtyRequested, Project businessProject) throws AxelorException  {
//		
//		ProductionOrder productionOrder = this.createProductionOrder(businessProject, false);
//		
//		this.addManufOrder(productionOrder, product, billOfMaterial, qtyRequested);
//		
//		return productionOrder.save();
//		
//	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public ProductionOrder addManufOrder(ProductionOrder productionOrder, Product product, BillOfMaterial billOfMaterial, BigDecimal qtyRequested) throws AxelorException  {
		
		BigDecimal qty = qtyRequested.divide(billOfMaterial.getQty());
		
		ManufOrder manufOrder = manufOrderService.generateManufOrder(
				product, 
				qty, 
				ManufOrderService.DEFAULT_PRIORITY, 
				ManufOrderService.IS_TO_INVOICE, 
				billOfMaterial.getCompany(), 
				billOfMaterial, 
				new LocalDateTime());
		
		productionOrder.addManufOrderListItem(manufOrder);
		
		return save(productionOrder);
		
	}
	
	
}
