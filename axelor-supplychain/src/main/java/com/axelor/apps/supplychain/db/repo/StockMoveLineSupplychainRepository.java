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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineStockRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychain;
import com.axelor.inject.Beans;
import java.util.Map;

public class StockMoveLineSupplychainRepository extends StockMoveLineStockRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long stockMoveLineId = (Long) json.get("id");
    StockMoveLine stockMoveLine = find(stockMoveLineId);
    StockMove stockMove = stockMoveLine.getStockMove();

    Map<String, Object> stockMoveLineMap = super.populate(json, context);
    if (stockMove != null && stockMove.getStatusSelect() == StockMoveRepository.STATUS_REALIZED) {
      Beans.get(StockMoveLineServiceSupplychain.class).setInvoiceStatus(stockMoveLine);
      json.put(
          "availableStatus",
          stockMoveLine.getProduct() != null && stockMoveLine.getProduct().getStockManaged()
              ? stockMoveLine.getAvailableStatus()
              : null);
      json.put("availableStatusSelect", stockMoveLine.getAvailableStatusSelect());
    }
    return stockMoveLineMap;
  }
}
