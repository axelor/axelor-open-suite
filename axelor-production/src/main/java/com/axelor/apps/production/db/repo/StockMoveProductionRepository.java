/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.db.repo;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.db.repo.StockMoveSupplychainRepository;

public class StockMoveProductionRepository extends StockMoveSupplychainRepository {

  @Override
  public StockMove copy(StockMove entity, boolean deep) {
    StockMove copy = super.copy(entity, deep);
    copy.setInManufOrder(null);
    copy.setOutManufOrder(null);
    copy.setInOperationOrder(null);
    if (copy.getStockMoveLineList() != null) {
      for (StockMoveLine stockMoveLine : copy.getStockMoveLineList()) {
        stockMoveLine.setProducedManufOrder(null);
        stockMoveLine.setConsumedManufOrder(null);
        stockMoveLine.setConsumedOperationOrder(null);
      }
    }
    return copy;
  }
}
