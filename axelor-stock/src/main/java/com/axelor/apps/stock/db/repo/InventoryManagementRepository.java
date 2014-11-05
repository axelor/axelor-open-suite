package com.axelor.apps.stock.db.repo;

import org.joda.time.DateTime;

import com.axelor.apps.stock.db.Inventory;

public class InventoryManagementRepository extends InventoryRepository {
	@Override
	public Inventory copy(Inventory entity, boolean deep) {
		entity.setStatusSelect(1);
		entity.setInventorySeq(null);
		entity.setDateT(DateTime.now());
		return super.copy(entity, deep);
	}
}
