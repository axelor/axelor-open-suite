package com.axelor.apps.stock.db.repo;

import com.axelor.apps.stock.db.StockMove;

public class StockMoveManagementRepository extends StockMoveRepository {
	@Override
	public StockMove copy(StockMove entity, boolean deep) {

		StockMove copy = super.copy(entity, deep);

		copy.setStatusSelect(1);
		copy.setStockMoveSeq(null);
		copy.setName(null);
		copy.setRealDate(null);

		return copy;
	}
}
