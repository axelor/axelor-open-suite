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

import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.SaleOrderLineService;
import com.axelor.apps.sale.service.SaleOrderLineTaxService;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseService;
import com.axelor.apps.supplychain.service.SaleOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.SaleOrderStockService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderServiceProductionImpl extends SaleOrderServiceSupplychainImpl {
	
	protected ProductionOrderSaleOrderService productionOrderSaleOrderService;
	
	@Inject
	public SaleOrderServiceProductionImpl(SaleOrderLineService saleOrderLineService, SaleOrderLineTaxService saleOrderLineTaxService,
			SequenceService sequenceService, PartnerService partnerService, PartnerRepository partnerRepo, SaleOrderRepository saleOrderRepo,
			GeneralService generalService, UserService userService, SaleOrderStockService saleOrderStockService, SaleOrderPurchaseService saleOrderPurchaseService,
			ProductionOrderSaleOrderService productionOrderSaleOrderService) {
		
		super(saleOrderLineService, saleOrderLineTaxService, sequenceService,partnerService, partnerRepo, saleOrderRepo, generalService,
				userService, saleOrderStockService, saleOrderPurchaseService);
		
		this.productionOrderSaleOrderService = productionOrderSaleOrderService;
		
	}
	
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void confirmSaleOrder(SaleOrder saleOrder) throws Exception  {

		super.confirmSaleOrder(saleOrder);
		
		if(general.getProductionOrderGenerationAuto())  {
			productionOrderSaleOrderService.generateProductionOrder(saleOrder);
		}
		
	}
	
	
}





