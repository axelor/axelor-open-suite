package com.axelor.apps.stock.db.repo;

import org.joda.time.DateTime;

import com.axelor.apps.stock.db.Inventory;

public class InventoryManagementRepository extends InventoryRepository {
	@Override
	public Inventory copy(Inventory entity, boolean deep) {
		
		Inventory copy = super.copy(entity, deep);
		
		copy.setStatusSelect(STATUS_DRAFT);
		copy.setInventorySeq(null);
		copy.setDateT(DateTime.now());
		return copy;
	}
}
