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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class OpportunitySaleOrderServiceImpl implements OpportunitySaleOrderService  {

	@Inject
	protected SaleOrderServiceImpl saleOrderService;
	
	@Inject
	protected SaleOrderRepository saleOrderRepo;

	@Inject
	protected GeneralService generalService;

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public SaleOrder createSaleOrderFromOpportunity(Opportunity opportunity) throws AxelorException  {

		SaleOrder saleOrder = saleOrderService.createSaleOrder(opportunity.getUser(), opportunity.getCompany(), null, opportunity.getCurrency(), null, opportunity.getName(), null,
				generalService.getTodayDate(), opportunity.getPartner().getSalePriceList(), opportunity.getPartner(), opportunity.getTeam());

		saleOrderRepo.save(saleOrder);

		return saleOrder;
	}


}
