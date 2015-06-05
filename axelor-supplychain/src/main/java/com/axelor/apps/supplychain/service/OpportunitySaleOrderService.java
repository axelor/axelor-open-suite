package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.db.Repository;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface OpportunitySaleOrderService extends Repository<Opportunity>{
	@Transactional
	public SaleOrder createSaleOrderFromOpportunity(Opportunity opportunity) throws AxelorException;
	
	@Transactional
	public Partner createClientFromLead(Opportunity opportunity) throws AxelorException;
}
