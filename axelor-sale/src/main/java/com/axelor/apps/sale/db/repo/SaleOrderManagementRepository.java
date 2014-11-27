package com.axelor.apps.sale.db.repo;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;

public class SaleOrderManagementRepository extends SaleOrderRepository {
	
	@Override
	public SaleOrder copy(SaleOrder entity, boolean deep) {
		entity.setStatusSelect(ISaleOrder.STATUS_DRAFT);
		entity.setSaleOrderSeq(null);
		entity.clearBatchSet();
		entity.setImportId(null);
		entity.setCreationDate(GeneralService.getTodayDate());
		entity.setValidationDate(null);
		entity.setValidatedByUser(null);
		entity.setOrderDate(null);
		entity.setOrderNumber(null);
		
		return super.copy(entity, deep);
	}
}
