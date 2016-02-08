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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.stock.service.MinStockRulesService;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.service.MrpLineServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class MrpLineServiceProductionImpl extends MrpLineServiceImpl  {
	
	protected ManufOrderService manufOrderService;

	
	@Inject
	public MrpLineServiceProductionImpl(GeneralService generalService, UserService userService, PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychainImpl, 
			PurchaseOrderLineService purchaseOrderLineService, PurchaseOrderRepository purchaseOrderRepo, ManufOrderService manufOrderService, 
			ProductionOrderRepository productionOrderRepo, MinStockRulesService minStockRulesService)  {
		
		super(generalService, userService, purchaseOrderServiceSupplychainImpl, purchaseOrderLineService, purchaseOrderRepo, minStockRulesService);
		this.manufOrderService = manufOrderService;
		
	}
	
	@Override
	public void generateProposal(MrpLine mrpLine) throws AxelorException  {
		
		super.generateProposal(mrpLine);
		
		if(mrpLine.getMrpLineType().getElementSelect() == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL)  {
			
			this.generateManufacturingProposal(mrpLine);
			
		}
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	protected void generateManufacturingProposal(MrpLine mrpLine) throws AxelorException  {
		
		Product product = mrpLine.getProduct();
		
		manufOrderService.generateManufOrder(product, mrpLine.getQty(), 
			ManufOrderService.DEFAULT_PRIORITY, 
			ManufOrderService.IS_TO_INVOICE, 
			null, mrpLine.getMaturityDate().toDateTimeAtStartOfDay().toLocalDateTime()); // TODO compute the time to produce to put the manuf order at the correct day
		
	}
	
	@Override
	protected String computeReleatedName(Model model)  {
		
		if(model instanceof ManufOrder)  {
			
			return ((ManufOrder) model).getManufOrderSeq();
			
		}
		else if(model instanceof OperationOrder)  {
			
			return ((OperationOrder) model).getName();
		}
		else  {
			return super.computeReleatedName(model);
		}
		
	}

}
