package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.db.ManufOrder;

public class ManufOrderManagementRepository extends ManufOrderRepository {
	@Override
	public ManufOrder copy(ManufOrder entity, boolean deep) {
		entity.setStatusSelect(1);
		entity.setManufOrderSeq(null);
		entity.setRealEndDateT(null);
		entity.setRealEndDateT(null);
		entity.setInStockMove(null);
		entity.setOutStockMove(null);
		entity.setWasteStockMove(null);
		entity.setToConsumeProdProductList(null);
		entity.setConsumedStockMoveLineList(null);
		entity.setDiffConsumeProdProductList(null);
		entity.setToProduceProdProductList(null);
		entity.setProducedStockMoveLineList(null);
		entity.setWasteProdProductList(null);
		return super.copy(entity, deep);
	}
}
