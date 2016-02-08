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

		if (copy.getSaleOrderLineList() != null){
			for (SaleOrderLine saleOrderLine : copy.getSaleOrderLineList()) {
				if (saleOrderLine.getSubscriptionList() != null){
					for (Subscription subscription : saleOrderLine.getSubscriptionList()) {
						subscription.setInvoiced(false);
					}
				}
			}
		}

		return copy;
	}
}
