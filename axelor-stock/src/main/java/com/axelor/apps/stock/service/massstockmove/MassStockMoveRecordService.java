package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.MassStockMove;

public interface MassStockMoveRecordService {
  void onNew(MassStockMove massStockMove);
}
