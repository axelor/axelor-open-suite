package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderManagementRepository;
import com.axelor.apps.supplychain.db.Subscription;

public class SaleOrderSupplychainRepository extends SaleOrderManagementRepository {

	@Override
	public SaleOrder copy(SaleOrder entity, boolean deep) {

		SaleOrder copy = super.copy(entity, deep);

		copy.setShipmentDate(null);
		copy.setDeliveryState(STATE_NOT_DELIVERED);
		copy.setAmountInvoiced(null);

		for (SaleOrderLine saleOrderLine : copy.getSaleOrderLineList()) {
			for (Subscription subscription : saleOrderLine.getSubscriptionList()) {
				subscription.setInvoiced(false);
			}
		}

		return copy;
	}
}
