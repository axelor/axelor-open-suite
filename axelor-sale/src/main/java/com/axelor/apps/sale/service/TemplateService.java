package com.axelor.apps.sale.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.google.inject.persist.Transactional;

public class TemplateService extends SaleOrderRepository{
	
	@Transactional
	public SaleOrder createSaleOrder(SaleOrder context){
		SaleOrder copy = copy(context, true);
		copy.setTemplate(false);
		copy.setTemplateUser(null);
		save(copy);
		return copy;
	}
}
