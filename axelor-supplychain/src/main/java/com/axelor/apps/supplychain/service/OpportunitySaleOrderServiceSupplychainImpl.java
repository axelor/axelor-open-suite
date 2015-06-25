package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.OpportunitySaleOrderServiceImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class OpportunitySaleOrderServiceSupplychainImpl extends OpportunitySaleOrderServiceImpl {

	@Inject
	private SaleOrderServiceSupplychainImpl saleOrderServiceSupplychainImpl;

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public SaleOrder createSaleOrderFromOpportunity(Opportunity opportunity) throws AxelorException{
		
		SaleOrder saleOrder = saleOrderServiceSupplychainImpl.createSaleOrder(opportunity.getUser(), opportunity.getCompany(), null, opportunity.getCurrency(), null, opportunity.getName(), null, 
				null, GeneralService.getTodayDate(), opportunity.getPartner().getSalePriceList(), opportunity.getPartner(), opportunity.getTeam());

		save(opportunity);

		return saleOrder;
	}

}
