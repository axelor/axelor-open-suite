/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.production.db.IWorkCenter;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.db.CostSheetLine;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdResidualProduct;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CostSheetServiceImpl implements CostSheetService  {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected UnitConversionService unitConversionService;
	protected CostSheetLineService costSheetLineService;
	protected BillOfMaterialRepository billOfMaterialRepo;
	
	protected Unit hourUnit;
	protected Unit cycleUnit;
	protected boolean manageResidualProductOnBom;
	protected boolean subtractProdResidualOnCostSheet;
	
	protected CostSheet costSheet;
	
	@Inject
	public CostSheetServiceImpl(GeneralService generalService, UnitConversionService unitConversionService, CostSheetLineService costSheetLineService, BillOfMaterialRepository billOfMaterialRepo)  {
		
		this.unitConversionService = unitConversionService;
		this.costSheetLineService = costSheetLineService;
		this.billOfMaterialRepo = billOfMaterialRepo;
		
		General general = generalService.getGeneral();
		this.hourUnit = general.getUnitHours();
		this.cycleUnit = general.getCycleUnit();
		this.manageResidualProductOnBom = general.getManageResidualProductOnBom();
		this.subtractProdResidualOnCostSheet = general.getSubtractProdResidualOnCostSheet();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public CostSheet computeCostPrice(BillOfMaterial billOfMaterial) throws AxelorException  {

		costSheet = new CostSheet();
		
		CostSheetLine producedCostSheetLine = costSheetLineService.createProducedProductCostSheetLine(billOfMaterial.getProduct(), billOfMaterial.getUnit(), billOfMaterial.getQty());
		
		costSheet.addCostSheetLineListItem(producedCostSheetLine);
		
		this._computeCostPrice(billOfMaterial, 0, producedCostSheetLine);
		
		this.computeResidualProduct(billOfMaterial);
		
		billOfMaterial.setCostPrice(this.computeCostPrice(costSheet));
		
		billOfMaterial.addCostSheetListItem(costSheet);
		
		billOfMaterialRepo.save(billOfMaterial);
		
		return costSheet;
		
	}
	
	
	protected void computeResidualProduct(BillOfMaterial billOfMaterial) throws AxelorException  {
		
		if(this.manageResidualProductOnBom && billOfMaterial.getProdResidualProductList() != null)  {
			
			for(ProdResidualProduct prodResidualProduct : billOfMaterial.getProdResidualProductList())  {
				
				CostSheetLine costSheetLine = costSheetLineService.createResidualProductCostSheetLine(prodResidualProduct.getProduct(), prodResidualProduct.getUnit(), prodResidualProduct.getQty());
				
				costSheet.addCostSheetLineListItem(costSheetLine);
						
			}
			
		}
		
	}
	
	
	protected BigDecimal computeCostPrice(CostSheet costSheet)  {
		
		BigDecimal costPrice = BigDecimal.ZERO;
		
		if(costSheet.getCostSheetLineList() != null)  {
			for(CostSheetLine costSheetLine : costSheet.getCostSheetLineList())  {
				
				if(costSheetLine.getCostSheetLineList() != null && !costSheetLine.getCostSheetLineList().isEmpty())  {
					costPrice = costPrice.add(this.computeCostPrice(costSheetLine));
				}
				else  {
					costPrice = costPrice.add(costSheetLine.getCostPrice());
				}
			}
		}
		
		costSheet.setCostPrice(costPrice);
		
		return costPrice;
	}
	
	
	protected BigDecimal computeCostPrice(CostSheetLine parentCostSheetLine)  {
		
		BigDecimal costPrice = BigDecimal.ZERO;
		
		if(parentCostSheetLine.getCostSheetLineList() != null)  {
			for(CostSheetLine costSheetLine : parentCostSheetLine.getCostSheetLineList())  {
				
				if(costSheetLine.getCostSheetLineList() != null && !costSheetLine.getCostSheetLineList().isEmpty())  {
					costPrice = costPrice.add(this.computeCostPrice(costSheetLine));
				}
				else  {
					costPrice = costPrice.add(costSheetLine.getCostPrice());
				}
			}
		}
		
		parentCostSheetLine.setCostPrice(costPrice);
		
		return costPrice;
	}


	protected void _computeCostPrice(BillOfMaterial billOfMaterial, int bomLevel, CostSheetLine parentCostSheetLine) throws AxelorException  {

		bomLevel++;
		
		// Cout des composants
		this._computeToConsumeProduct(billOfMaterial, bomLevel, parentCostSheetLine);

		// Cout des operations
		this._computeProcess(billOfMaterial.getProdProcess(), billOfMaterial.getQty(), billOfMaterial.getProduct().getUnit(), bomLevel, parentCostSheetLine);

	}


	protected void _computeToConsumeProduct(BillOfMaterial billOfMaterial, int bomLevel, CostSheetLine parentCostSheetLine) throws AxelorException  {

		if(billOfMaterial.getBillOfMaterialList() != null)  {

			for(BillOfMaterial billOfMaterialLine : billOfMaterial.getBillOfMaterialList())  {

				Product product = billOfMaterialLine.getProduct();

				if(product != null)  {
					
					CostSheetLine costSheetLine = costSheetLineService.createConsumedProductCostSheetLine(product, billOfMaterialLine.getUnit(), bomLevel, parentCostSheetLine, billOfMaterialLine.getQty());
					
					if(!billOfMaterialLine.getIsRawMaterial())  {
						this._computeCostPrice(billOfMaterialLine, bomLevel, costSheetLine);
					}
				}
			}
		}

	}


	protected void _computeProcess(ProdProcess prodProcess, BigDecimal producedQty, Unit pieceUnit, int bomLevel, CostSheetLine parentCostSheetLine) throws AxelorException  {

		if(prodProcess != null && prodProcess.getProdProcessLineList() != null)  {

			for(ProdProcessLine prodProcessLine : prodProcess.getProdProcessLineList())  {

				WorkCenter workCenter = prodProcessLine.getWorkCenter();

				if(workCenter != null)  {

					int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();

					if(workCenterTypeSelect == IWorkCenter.WORK_CENTER_HUMAN || workCenterTypeSelect == IWorkCenter.WORK_CENTER_BOTH)  {

						this._computeHumanResourceCost(workCenter, prodProcessLine.getPriority(), bomLevel, parentCostSheetLine);

					}
					if(workCenterTypeSelect == IWorkCenter.WORK_CENTER_MACHINE || workCenterTypeSelect == IWorkCenter.WORK_CENTER_BOTH)  {

						this._computeMachineCost(prodProcessLine, producedQty, pieceUnit, bomLevel, parentCostSheetLine);

					}

				}
			}
		}

	}



	protected void _computeHumanResourceCost(WorkCenter workCenter, int priority, int bomLevel, CostSheetLine parentCostSheetLine) throws AxelorException  {

		if(workCenter.getProdHumanResourceList() != null)  {

			for(ProdHumanResource prodHumanResource : workCenter.getProdHumanResourceList())  {

				this._computeHumanResourceCost(prodHumanResource, priority, bomLevel, parentCostSheetLine);
			}

		}

	}
	
	protected void _computeHumanResourceCost(ProdHumanResource prodHumanResource, int priority, int bomLevel, CostSheetLine parentCostSheetLine) throws AxelorException  {

		BigDecimal costPerHour = BigDecimal.ZERO;
		
		if(prodHumanResource.getProduct() != null)  {

			Product product = prodHumanResource.getProduct();

			costPerHour = unitConversionService.convert(hourUnit, product.getUnit(), product.getCostPrice());

		}
		
		BigDecimal durationHours = BigDecimal.valueOf(prodHumanResource.getDuration()).divide(BigDecimal.valueOf(3600), 5, RoundingMode.HALF_EVEN);
			
		costSheetLineService.createWorkCenterCostSheetLine(prodHumanResource.getWorkCenter(), priority, bomLevel, parentCostSheetLine, 
				durationHours, costPerHour.multiply(durationHours), hourUnit);

	}

	
	protected void _computeMachineCost(ProdProcessLine prodProcessLine, BigDecimal producedQty, Unit pieceUnit, int bomLevel, CostSheetLine parentCostSheetLine)  {

		WorkCenter workCenter = prodProcessLine.getWorkCenter();

		int costType = workCenter.getCostTypeSelect();

		if(costType == IWorkCenter.COST_PER_CYCLE)  {
			
			costSheetLineService.createWorkCenterCostSheetLine(workCenter, prodProcessLine.getPriority(), bomLevel, parentCostSheetLine, 
					this.getNbCycle(producedQty, prodProcessLine.getCapacityPerCycle()), workCenter.getCostAmount(), cycleUnit);

		}
		else if(costType == IWorkCenter.COST_PER_HOUR)  {

			BigDecimal qty = new BigDecimal(prodProcessLine.getDurationPerCycle()).divide(new BigDecimal(3600), BigDecimal.ROUND_HALF_EVEN).multiply(this.getNbCycle(producedQty, prodProcessLine.getCapacityPerCycle()));
			BigDecimal costPrice = workCenter.getCostAmount().multiply(qty);
			
			costSheetLineService.createWorkCenterCostSheetLine(workCenter, prodProcessLine.getPriority(), bomLevel, parentCostSheetLine, qty, costPrice, hourUnit);

		}
		else if(costType == IWorkCenter.COST_PER_PIECE)  {

			BigDecimal costPrice = workCenter.getCostAmount().multiply(producedQty);
			
			costSheetLineService.createWorkCenterCostSheetLine(workCenter, prodProcessLine.getPriority(), bomLevel, parentCostSheetLine, producedQty, costPrice, pieceUnit);

		}


	}

	protected BigDecimal getNbCycle(BigDecimal producedQty, BigDecimal capacityPerCycle)  {
		
		if(capacityPerCycle.compareTo(BigDecimal.ZERO) == 0)  {
			return producedQty;
		}
		
		return producedQty.divide(capacityPerCycle, RoundingMode.CEILING);
		
	}

}
