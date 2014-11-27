package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderManagementRepository;

public class SaleOrderSupplychainRepository extends SaleOrderManagementRepository {
	
	@Override
	public SaleOrder copy(SaleOrder entity, boolean deep) {
		entity.setShipmentDate(null);
		entity.clearInvoiceSet();
		return super.copy(entity, deep);
	}
}
