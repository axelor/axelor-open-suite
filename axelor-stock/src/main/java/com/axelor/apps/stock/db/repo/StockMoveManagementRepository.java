package com.axelor.apps.stock.db.repo;

import com.axelor.apps.stock.db.StockMove;

public class StockMoveManagementRepository extends StockMoveRepository {
 @Override
public StockMove copy(StockMove entity, boolean deep) {
	entity.setStatusSelect(1);
	entity.setStockMoveSeq(null);
	entity.setName(null);
	entity.setRealDate(null);
	return super.copy(entity, deep);
}
}
