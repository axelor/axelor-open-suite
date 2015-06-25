package com.axelor.apps.sale.service;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class OpportunitySaleOrderServiceImpl extends OpportunityRepository implements OpportunitySaleOrderService  {

	@Inject
	private SaleOrderServiceImpl saleOrderService;

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public SaleOrder createSaleOrderFromOpportunity(Opportunity opportunity) throws AxelorException  {
		
		SaleOrder saleOrder = saleOrderService.createSaleOrder(opportunity.getUser(), opportunity.getCompany(), null, opportunity.getCurrency(), null, opportunity.getName(), null, 
				GeneralService.getTodayDate(), opportunity.getPartner().getSalePriceList(), opportunity.getPartner(), opportunity.getTeam());

		save(opportunity);

		return saleOrder;
	}

	
}
