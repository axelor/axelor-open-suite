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

import com.axelor.app.production.db.IProdResource;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdResource;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;

public class BillOfMaterialServiceBusinessImpl extends BillOfMaterialServiceImpl {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	@Override
	protected BigDecimal _computeProcess(ProdProcess prodProcess) throws AxelorException  {
		
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
	
	
	
	private BigDecimal _computeHumanResourceCost(ProdResource prodResource) throws AxelorException  {
		
		BigDecimal costPrice = BigDecimal.ZERO;
		
		UnitRepository unitRepository = Beans.get(UnitRepository.class);
		
		if(prodResource.getProdHumanResourceList() != null)  {
			
			for(ProdHumanResource prodHumanResource : prodResource.getProdHumanResourceList())  {
				
				if(prodHumanResource.getEmployee() != null)  {
					
					BigDecimal costPerMin = unitConversionService.convert(unitRepository.findByCode(UNIT_MIN_CODE), unitRepository.findByCode(UNIT_DAY_CODE), prodHumanResource.getEmployee().getDailySalaryCost());
					
					costPrice = costPrice.add((costPerMin).multiply(new BigDecimal(prodHumanResource.getDuration()/60)));
					
				}
				else if(prodHumanResource.getProduct() != null)  {
					
					Product product = prodHumanResource.getProduct();
					
					BigDecimal costPerMin = unitConversionService.convert(unitRepository.findByCode(UNIT_MIN_CODE), product.getUnit(), product.getCostPrice());
					
					costPrice = costPrice.add((costPerMin).multiply(new BigDecimal(prodHumanResource.getDuration()/60)));
					
				}
			}
			
		}
		
		logger.debug("Human resource cost : {} (Resource : {})",costPrice, prodResource.getName());
		
		return costPrice;
	}
	
	
	
	
}
