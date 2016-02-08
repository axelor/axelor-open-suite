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
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.production.db.IManufOrder;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.MinStockRules;
import com.axelor.apps.stock.db.repo.LocationLineRepository;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.apps.stock.db.repo.MinStockRulesRepository;
import com.axelor.apps.stock.service.MinStockRulesService;
import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.MrpLineOrigin;
import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.service.MrpLineService;
import com.axelor.apps.supplychain.service.MrpServiceImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class MrpServiceProductionImpl extends MrpServiceImpl  {
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	protected BillOfMaterialRepository billOfMaterialRepository;
	protected ManufOrderRepository manufOrderRepository;

	
	@Inject
	public MrpServiceProductionImpl(GeneralService generalService, MrpRepository mrpRepository, LocationRepository locationRepository, 
			ProductRepository productRepository, LocationLineRepository locationLineRepository, MrpLineTypeRepository mrpLineTypeRepository,
			PurchaseOrderLineRepository purchaseOrderLineRepository, SaleOrderLineRepository saleOrderLineRepository, MrpLineRepository mrpLineRepository,
			MinStockRulesService minStockRulesService, MrpLineService mrpLineService, MrpForecastRepository mrpForecastRepository,
			BillOfMaterialRepository billOfMaterialRepository, ManufOrderRepository manufOrderRepository)  {
		
		
		super(generalService, mrpRepository, locationRepository, productRepository, locationLineRepository, mrpLineTypeRepository, 
				purchaseOrderLineRepository, saleOrderLineRepository, mrpLineRepository, minStockRulesService, mrpLineService, mrpForecastRepository);
		
		this.billOfMaterialRepository = billOfMaterialRepository;
		this.manufOrderRepository = manufOrderRepository;
		
	}
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	protected void completeMrp(Mrp mrp) throws AxelorException  {
		
		super.completeMrp(mrp);
		
		this.createManufOrderMrpLines();
		
		mrpRepository.save(mrp);
	}
	
	
	// Manufactoring order AND manufactoring order need
	protected void createManufOrderMrpLines() throws AxelorException  {
		
		MrpLineType manufOrderMrpLineType = this.getMrpLineType(MrpLineTypeRepository.ELEMENT_MANUFACTURING_ORDER);
		MrpLineType manufOrderNeedMrpLineType = this.getMrpLineType(MrpLineTypeRepository.ELEMENT_MANUFACTURING_ORDER_NEED);
		
		List<ManufOrder> manufOrderList = manufOrderRepository.all()
				.filter("self.product in (?1) AND self.prodProcess.location in (?2) "
						+ "AND self.statusSelect != ?3 AND self.plannedStartDateT > ?4", 
						this.productMap.keySet(), this.locationList, IManufOrder.STATUS_FINISHED, today.toDateTimeAtStartOfDay()).fetch();
		
		for(ManufOrder manufOrder : manufOrderList)  {
		
			Location location = manufOrder.getProdProcess().getLocation();
			
			for(ProdProduct prodProduct : manufOrder.getToProduceProdProductList())  {
				
				Product product = prodProduct.getProduct();
				
				LocalDate maturityDate = null;
				
				if(manufOrder.getPlannedEndDateT() != null)  {  maturityDate = manufOrder.getPlannedEndDateT().toLocalDate();  }
				else  {  maturityDate = manufOrder.getPlannedStartDateT().toLocalDate();  }
				
				if(this.isBeforeEndDate(maturityDate) && this.isMrpProduct(product))  {

					mrp.addMrpLineListItem(this.createMrpLine(product, manufOrderMrpLineType, prodProduct.getQty(), 
						maturityDate, BigDecimal.ZERO, location, manufOrder));
				}
				
			}

			if(manufOrder.getIsConsProOnOperation())  {
				for(OperationOrder operationOrder : manufOrder.getOperationOrderList())  {
	 				for(ProdProduct prodProduct : operationOrder.getToConsumeProdProductList())  {
						
	 					Product product = prodProduct.getProduct();
	 					
	 					if(this.isMrpProduct(product))  {
	 						
	 						mrp.addMrpLineListItem(this.createMrpLine(prodProduct.getProduct(), manufOrderNeedMrpLineType, prodProduct.getQty(), 
									operationOrder.getPlannedStartDateT().toLocalDate(), BigDecimal.ZERO, location, operationOrder));
	 					}
					}
				}
			}
			else  {
				for(ProdProduct prodProduct : manufOrder.getToConsumeProdProductList())  {
					
 					Product product = prodProduct.getProduct();
					
					if(this.isMrpProduct(product))  {
						mrp.addMrpLineListItem(this.createMrpLine(product, manufOrderNeedMrpLineType, prodProduct.getQty(), 
								manufOrder.getPlannedStartDateT().toLocalDate(), BigDecimal.ZERO, location, manufOrder));
					}
				}
			}
			
		}
	}
	
	@Override
	protected void createProposalMrpLine(Product product, MrpLineType mrpLineType, BigDecimal reorderQty, Location location, LocalDate maturityDate, List<MrpLineOrigin> mrpLineOriginList, String relatedToSelectName) throws AxelorException  {
		
		super.createProposalMrpLine(product, mrpLineType, reorderQty, location, maturityDate, mrpLineOriginList, relatedToSelectName);
		
		BillOfMaterial defaultBillOfMaterial = product.getDefaultBillOfMaterial();
		
		if(mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL && defaultBillOfMaterial != null)  {
			
			MrpLineType manufProposalNeedMrpLineType = this.getMrpLineType(MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL_NEED);
			
			for(BillOfMaterial billOfMaterial : defaultBillOfMaterial.getBillOfMaterialList())  {
				
				Product subProduct = billOfMaterial.getProduct();
				
				if(this.isMrpProduct(subProduct))  {
					//TODO take the time to do the Manuf order (use machine planning)
					super.createProposalMrpLine(subProduct, manufProposalNeedMrpLineType, reorderQty.multiply(billOfMaterial.getQty()), location, maturityDate, mrpLineOriginList, relatedToSelectName);
				}
			}
			
		}
		
	}
	

	@Override
	protected MrpLineType getMrpLineTypeForProposal(MinStockRules minStockRules) throws AxelorException  {
		
		// TODO manage the default value in general administration
		
		if(minStockRules != null && minStockRules.getOrderAlertSelect() == MinStockRulesRepository.ORDER_ALERT_PRODUCTION_ORDER)  {
			return this.getMrpLineType(MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL);
		}
		
		return this.getMrpLineType(MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL);
		
	}
	
	
	@Override
	protected boolean isProposalElement(MrpLineType mrpLineType)  {
		
		if(mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL  
			|| mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL
			|| mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL_NEED)  {
		
			return true;
			
		}
		
		return false;
	}
	

	@Override
	protected void assignProductAndLevel(Product product)  {
		
		log.debug("Add of the product : {}", product.getFullName());
		this.productMap.put(product,  this.getMaxLevel(product, 0));
		
		if(product.getDefaultBillOfMaterial() != null)  {
			this.assignProductLevel(product.getDefaultBillOfMaterial(), 0);
		}
		
	}
	
	public int getMaxLevel(Product product, int level)  {
		
		if(this.productMap.containsKey(product))  {
			return Math.max(level, this.productMap.get(product));
		}
		
		return level;
	}

	
	/**
	 * Update the level of Bill of material. The highest for each product (0: product with parent, 1: product with a parent, 2: product with a parent that have a parent, ...)
	 * @param billOfMaterial
	 * @param level
	 */
	protected void assignProductLevel(BillOfMaterial billOfMaterial, int level)  {
		
		if(billOfMaterial.getBillOfMaterialList() == null || billOfMaterial.getBillOfMaterialList().isEmpty() || level > 100)  {
		
			Product subProduct = billOfMaterial.getProduct();
			
			log.debug("Add of the sub product : {} for the level : {} ", subProduct.getFullName(), level);
			this.productMap.put(subProduct, this.getMaxLevel(subProduct, level));

		}
		else  {
		
			level = level + 1;

			for(BillOfMaterial subBillOfMaterial : billOfMaterial.getBillOfMaterialList())  {
				
				Product subProduct = subBillOfMaterial.getProduct();
					
				if(this.isMrpProduct(subProduct))  {
					this.assignProductLevel(subBillOfMaterial, level);
					
					if(subProduct.getDefaultBillOfMaterial() != null)  {
						this.assignProductLevel(subProduct.getDefaultBillOfMaterial(), level);
					}
				}

			}
		}
	}
	
	
	
	
}



















