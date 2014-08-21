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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.production.db.IProdResource;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdResource;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BillOfMaterialService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject 
	private UnitConversionService unitConversionService;
	
	@Inject
	private ProductService productService;
	
	static final String UNIT_MIN_CODE = "MIN";
	
	static final String UNIT_DAY_CODE = "JR";
	
	
	public List<BillOfMaterial> getBillOfMaterialList(Product product)  {
		
		return(List<BillOfMaterial>) BillOfMaterial.filter("self.product = ?1", product).fetch();
		
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void computeCostPrice(BillOfMaterial billOfMaterial) throws AxelorException  {
		
		billOfMaterial.setCostPrice(this._computeCostPrice(billOfMaterial).setScale(5, BigDecimal.ROUND_HALF_EVEN));
		
		billOfMaterial.getProduct().setCostPrice(billOfMaterial.getCostPrice());
		
		billOfMaterial.save();
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateProductCostPrice(BillOfMaterial billOfMaterial) throws AxelorException  {
		
		Product product = billOfMaterial.getProduct();
		
		product.setCostPrice(billOfMaterial.getCostPrice());
		
		productService.updateSalePrice(product);
		
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
						BigDecimal unitPrice = unitConversionService.convert(product.getUnit(), billOfMaterialLine.getUnit(), product.getCostPrice());
						costPrice = costPrice.add(unitPrice.multiply(billOfMaterialLine.getQty()));
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
						
//						costPrice = costPrice.add(this._computeHumanResourceCost(prodResource));
						
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
	
//	
//	private BigDecimal _computeHumanResourceCost(ProdResource prodResource) throws AxelorException  {
//		
//		BigDecimal costPrice = BigDecimal.ZERO;
//		
//		if(prodResource.getProdHumanResourceList() != null)  {
//			
//			for(ProdHumanResource prodHumanResource : prodResource.getProdHumanResourceList())  {
//				
//				if(prodHumanResource.getEmployee() != null)  {
//					
//					BigDecimal costPerMin = unitConversionService.convert(Unit.findByCode(UNIT_MIN_CODE), Unit.findByCode(UNIT_DAY_CODE), prodHumanResource.getEmployee().getDailySalaryCost());
//					
//					costPrice = costPrice.add((costPerMin).multiply(new BigDecimal(prodHumanResource.getDuration()/60)));
//					
//				}
//				else if(prodHumanResource.getProduct() != null)  {
//					
//					Product product = prodHumanResource.getProduct();
//					
//					BigDecimal costPerMin = unitConversionService.convert(Unit.findByCode(UNIT_MIN_CODE), product.getUnit(), product.getCostPrice());
//					
//					costPrice = costPrice.add((costPerMin).multiply(new BigDecimal(prodHumanResource.getDuration()/60)));
//					
//				}
//			}
//			
//		}
//		
//		logger.debug("Human resource cost : {} (Resource : {})",costPrice, prodResource.getName());
//		
//		return costPrice;
//	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public BillOfMaterial customizeBillOfMaterial(SaleOrderLine saleOrderLine)  {
		
		BillOfMaterial billOfMaterial = saleOrderLine.getBillOfMaterial();
		
		if(billOfMaterial != null)  {
			return JPA.copy(billOfMaterial, true);
		}
		
		return null;
		
	}
	
	
}
