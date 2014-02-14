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

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.production.db.IProdResource;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdResource;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BillOfMaterialService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject 
	private UnitConversionService unitConversionService;
	
	static final String UNIT_MIN_CODE = "MIN";
	
	static final String UNIT_DAY_CODE = "JR";
	
	
	public List<BillOfMaterial> getBillOfMaterialList(Product product)  {
		
		return BillOfMaterial.filter("self.product = ?1", product).fetch();
		
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void computeCostPrice(BillOfMaterial billOfMaterial) throws AxelorException  {
		
		billOfMaterial.setCostPrice(this._computeCostPrice(billOfMaterial).setScale(5, BigDecimal.ROUND_HALF_EVEN));
		
		billOfMaterial.getProduct().setCostPrice(billOfMaterial.getCostPrice());
		
		billOfMaterial.save();
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateProductCostPrice(BillOfMaterial billOfMaterial) throws AxelorException  {
		
		billOfMaterial.getProduct().setCostPrice(billOfMaterial.getCostPrice());
		
		billOfMaterial.save();
	}
	
	
	private BigDecimal _computeCostPrice(BillOfMaterial billOfMaterial) throws AxelorException  {
		
		BigDecimal costPrice = BigDecimal.ZERO;
		
		// Cout des composants
		costPrice = costPrice.add(this._computeToConsumeProduct(billOfMaterial));
		
		// Cout des operations
		costPrice = costPrice.add(this._computeProcess(billOfMaterial.getProdProcess()));
		
		return costPrice;
		
	}
	
	
	private BigDecimal _computeToConsumeProduct(BillOfMaterial billOfMaterial) throws AxelorException  {
		
		BigDecimal costPrice = BigDecimal.ZERO;
		
		if(billOfMaterial.getBillOfMaterialList() != null)  {
			
			for(BillOfMaterial billOfMaterialLine : billOfMaterial.getBillOfMaterialList())  {
				
				Product product = billOfMaterialLine.getProduct();
					
				if(product != null)  {
					if(billOfMaterialLine.getIsRawMaterial())  {
						costPrice = costPrice.add(product.getCostPrice());
					}
					else  {
						costPrice = costPrice.add(this._computeCostPrice(billOfMaterialLine));
					}
				}
			}
		}
		
		return costPrice;
	}
	
	
	
	private BigDecimal _computeProcess(ProdProcess prodProcess) throws AxelorException  {
		
		BigDecimal costPrice = BigDecimal.ZERO;
		
		if(prodProcess != null && prodProcess.getProdProcessLineList() != null)  {
			
			for(ProdProcessLine prodProcessLine : prodProcess.getProdProcessLineList())  {
				
				ProdResource prodResource = prodProcessLine.getProdResource();
				
				if(prodResource != null)  {

					int resourceType = prodResource.getResourceTypeSelect();
					
					if(resourceType == IProdResource.RESOURCE_HUMAN || resourceType == IProdResource.RESOURCE_BOTH)  {
						
						costPrice = costPrice.add(this._computeHumanResourceCost(prodResource));
						
					}
					if(resourceType == IProdResource.RESOURCE_MACHINE || resourceType == IProdResource.RESOURCE_BOTH)  {
						
						costPrice = costPrice.add(this._computeMachineCost(prodResource));
						
					}
					
				}
			}
		}
		
		return costPrice;
	}
	
	
	private BigDecimal _computeMachineCost(ProdResource prodResource)  {
		
		BigDecimal costPrice = BigDecimal.ZERO;
		
		int costType = prodResource.getCostTypeSelect();
		
		if(costType == IProdResource.COST_PER_CYCLE)  {
			
			costPrice = prodResource.getCostAmount();
		}
		else if(costType == IProdResource.COST_PER_HOUR)  {
			
			costPrice = (prodResource.getCostAmount().multiply(new BigDecimal(prodResource.getDurationPerCycle())).divide(new BigDecimal(3600), BigDecimal.ROUND_HALF_EVEN));
			
		}
		
		logger.debug("Machine cost : {} (Resource : {})",costPrice, prodResource.getName());
		
		return costPrice;
	}
	
	
	private BigDecimal _computeHumanResourceCost(ProdResource prodResource) throws AxelorException  {
		
		BigDecimal costPrice = BigDecimal.ZERO;
		
		if(prodResource.getProdHumanResourceList() != null)  {
			
			for(ProdHumanResource prodHumanResource : prodResource.getProdHumanResourceList())  {
				
				if(prodHumanResource.getEmployee() != null)  {
					
					BigDecimal costPerMin = unitConversionService.convert(Unit.findByCode(UNIT_MIN_CODE), Unit.findByCode(UNIT_DAY_CODE), prodHumanResource.getEmployee().getDailySalaryCost());
					
					costPrice = costPrice.add((costPerMin).multiply(new BigDecimal(prodHumanResource.getDuration()/60)));
					
				}
				else if(prodHumanResource.getProduct() != null)  {
					
					Product product = prodHumanResource.getProduct();
					
					BigDecimal costPerMin = unitConversionService.convert(Unit.findByCode(UNIT_MIN_CODE), product.getUnit(), product.getCostPrice());
					
					costPrice = costPrice.add((costPerMin).multiply(new BigDecimal(prodHumanResource.getDuration()/60)));
					
				}
			}
			
		}
		
		logger.debug("Human resource cost : {} (Resource : {})",costPrice, prodResource.getName());
		
		return costPrice;
	}
	
	
	
	
	
}
