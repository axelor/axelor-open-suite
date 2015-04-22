package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderManagementRepository;

public class SaleOrderSupplychainRepository extends SaleOrderManagementRepository {

	@Override
	public SaleOrder copy(SaleOrder entity, boolean deep) {

		SaleOrder copy = super.copy(entity, deep);

		copy.setShipmentDate(null);
		copy.clearInvoiceSet();
		copy.setDeliveryState(STATE_NOT_DELIVERED);

		return copy;
	}
}
