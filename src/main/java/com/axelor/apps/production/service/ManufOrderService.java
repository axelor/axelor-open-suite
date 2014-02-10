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
/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
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
 * Software distributed under the License is distributed on an “AS IS”
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


import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.production.db.IManufOrder;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.ProdRemains;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.meta.service.MetaTranslations;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ManufOrderService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public static int DEFAULT_PRIORITY = 10;
	public static int DEFAULT_PRIORITY_INTERVAL = 10;
	public static boolean IS_TO_INVOICE = false;
	
	@Inject
	private MetaTranslations metaTranslations;
	
	@Inject
	private SequenceService sequenceService;
	
	@Inject
	private OperationOrderService operationOrderService;
	
	@Inject
	private ManufOrderStockMoveService manufOrderStockMoveService;
	
	
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void propagateIsToInvoice(ManufOrder manufOrder) {

		logger.debug("{} is to invoice ? {}", manufOrder.getManufOrderSeq(), manufOrder.getIsToInvoice());
		
		boolean isToInvoice = manufOrder.getIsToInvoice();
		
		if(manufOrder.getOperationOrderList() != null)  {
			for(OperationOrder operationOrder : manufOrder.getOperationOrderList())  {
				
				operationOrder.setIsToInvoice(isToInvoice);
				
			}
		}
		
		manufOrder.save();
		
	}

	
	public ManufOrder generateManufOrder(BigDecimal qty, int priority, boolean isToInvoice, Company company,
			BillOfMaterial billOfMaterial, LocalDateTime plannedStartDateT) throws AxelorException  {
		
		ManufOrder manufOrder = this.createManufOrder(qty, priority, IS_TO_INVOICE, company, billOfMaterial, plannedStartDateT);
		
		
		if(manufOrder.getIsManagedConsumedProduct())  {
			//TODO
			
		}
		
		this.createToConsumeProdProductList(manufOrder, billOfMaterial);
		this.createToProduceProdProductList(manufOrder, billOfMaterial);
		
		manufOrder = manufOrder.save();
		
		manufOrder.setPlannedEndDateT(this.computePlannedEndDateT(manufOrder));
		
		manufOrderStockMoveService.createToConsumeStockMove(manufOrder);

		manufOrderStockMoveService.createToProduceStockMove(manufOrder);
		
		manufOrder.setStatusSelect(IManufOrder.STATUS_PLANNED);
		
		return manufOrder;
		
	}
	
	
	public void createToConsumeProdProductList(ManufOrder manufOrder, BillOfMaterial billOfMaterial)  {
		
		BigDecimal manufOrderQty = manufOrder.getQty();
		
		if(billOfMaterial.getBillOfMaterialList() != null)  {
			
			for(BillOfMaterial billOfMaterialLine : billOfMaterial.getBillOfMaterialList())  {
				
				manufOrder.addToConsumeProdProductListItem(
						new ProdProduct(billOfMaterialLine.getProduct(), billOfMaterialLine.getQty().multiply(manufOrderQty), billOfMaterialLine.getUnit()));
				
			}
			
		}
		
	}
	
	
	public void createToProduceProdProductList(ManufOrder manufOrder, BillOfMaterial billOfMaterial)  {
		
		BigDecimal manufOrderQty = manufOrder.getQty();
		
		// AJout du produit final
		manufOrder.addToProduceProdProductListItem(
				new ProdProduct(billOfMaterial.getProduct(), billOfMaterial.getQty().multiply(manufOrderQty), billOfMaterial.getUnit()));
		
		// Ajout des restes
		if(billOfMaterial.getProdRemainsList() != null)  {
			
			for(ProdRemains prodRemains : billOfMaterial.getProdRemainsList())  {
				
				manufOrder.addToProduceProdProductListItem(
						new ProdProduct(prodRemains.getProduct(), prodRemains.getQty().multiply(manufOrderQty), prodRemains.getUnit()));
				
			}
			
		}
		
	}
	
	
	
	
	
	
	
	public ManufOrder createManufOrder(BigDecimal qty, int priority, boolean isToInvoice, Company company,
			BillOfMaterial billOfMaterial, LocalDateTime plannedStartDateT) throws AxelorException  {
		
		logger.debug("Création d'un OF {}", priority);
		
		ProdProcess prodProcess = billOfMaterial.getProdProcess();
		
		ManufOrder manufOrder = new ManufOrder(
				isToInvoice, 
				qty,
				company, 
				this.getManufOrderSeq(), 
				priority, 
				this.isManagedConsumedProduct(billOfMaterial), 
				billOfMaterial, 
				prodProcess, 
				plannedStartDateT, 
				IManufOrder.STATUS_DRAFT);
		
		if(manufOrder.getIsManagedConsumedProduct())  {
			
			OperationOrder previousOperationOrder = null;
			
			for(ProdProcessLine prodProcessLine : this._sortProdProcessLineByPriority(prodProcess.getProdProcessLineList()))  {
				
				OperationOrder operationOrder = operationOrderService.createOperationOrder(
						manufOrder,
						prodProcessLine.getPriority(), 
						isToInvoice, 
						prodProcessLine.getProdResource(), 
						prodProcessLine.getProdResource(), 
						prodProcessLine, 
						this.getLastPlannedStartDateT(prodProcessLine, previousOperationOrder, plannedStartDateT));
				
				manufOrder.addOperationOrderListItem(operationOrder);
				
				previousOperationOrder = operationOrder;
				
			}
			
		}
		
		return manufOrder; 
		
	}
	
	
	public LocalDateTime getLastPlannedStartDateT(ProdProcessLine prodProcessLine, OperationOrder previousOperationOrder, LocalDateTime manufOrderPlannedStartDateT)  {
		
		if(previousOperationOrder == null)  {
			return manufOrderPlannedStartDateT;
		}
		else if(prodProcessLine.getPriority() == previousOperationOrder.getPriority())  {
			return previousOperationOrder.getPlannedStartDateT();
		}
		else  {
			return previousOperationOrder.getPlannedEndDateT();
		}
		
	}
	
	/**
	 * Trier une liste de ligne de règle de template
	 * 
	 * @param templateRuleLine
	 */
	private List<ProdProcessLine> _sortProdProcessLineByPriority(List<ProdProcessLine> prodProcessLineList){
		
		Collections.sort(prodProcessLineList, new Comparator<ProdProcessLine>() {
			
			@Override
			public int compare(ProdProcessLine ppl1, ProdProcessLine ppl2) {
				return ppl1.getPriority().compareTo(ppl2.getPriority());
			}
		});
		
		return prodProcessLineList;
	}
	
	
	public String getManufOrderSeq() throws AxelorException  {
		
		String seq = sequenceService.getSequence(IAdministration.MANUF_ORDER, false);
		
		if(seq == null)  {
			throw new AxelorException(metaTranslations.get(IExceptionMessage.MANUF_ORDER_SEQ), IException.CONFIGURATION_ERROR);
		}
		
		return seq;
	}

	public LocalDateTime computePlannedEndDateT(ManufOrder manufOrder)  {
		
		OperationOrder lastOperationOrder = OperationOrder.filter("self.manufOrder = ?1 ORDER BY self.plannedEndDateT DESC", manufOrder).fetchOne();
		
		if(lastOperationOrder != null)  {
			
			return lastOperationOrder.getPlannedEndDateT();
			
		}
		
		return manufOrder.getPlannedStartDateT();
		
	}
	
	
	public boolean isManagedConsumedProduct(BillOfMaterial billOfMaterial)  {
		
		if(billOfMaterial != null && billOfMaterial.getProdProcess() != null && billOfMaterial.getProdProcess().getProdProcessLineList() != null)  {
			for(ProdProcessLine prodProcessLine : billOfMaterial.getProdProcess().getProdProcessLineList())  {
				
				if((prodProcessLine.getToConsumeProdProductList() != null && !prodProcessLine.getToConsumeProdProductList().isEmpty()))  {
					
					return true;
					
				}

			}
		}
		
		return false;
		
	}
	
	
	
	

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void copyToProduce(ManufOrder manufOrder)  {
		
		if(manufOrder.getToProduceProdProductList() != null)  {
			
			for(ProdProduct prodProduct : manufOrder.getToProduceProdProductList())  {
				
				manufOrder.addProducedProdProductListItem(new ProdProduct(prodProduct.getProduct(), prodProduct.getQty(), prodProduct.getUnit()));

			}
			
		}
		
		manufOrder.save();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void copyToConsume(ManufOrder manufOrder)  {
		
		if(manufOrder.getToConsumeProdProductList() != null)  {
			
			for(ProdProduct prodProduct : manufOrder.getToConsumeProdProductList())  {
				
				manufOrder.addConsumedProdProductListItem(new ProdProduct(prodProduct.getProduct(), prodProduct.getQty(), prodProduct.getUnit()));

			}
			
		}
		
		manufOrder.save();
		
	}
	
}
