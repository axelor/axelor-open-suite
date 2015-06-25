package com.axelor.apps.sale.service;

import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.db.Repository;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface OpportunitySaleOrderService extends Repository<Opportunity>{
	
	@Transactional
	public SaleOrder createSaleOrderFromOpportunity(Opportunity opportunity) throws AxelorException;
	
}
