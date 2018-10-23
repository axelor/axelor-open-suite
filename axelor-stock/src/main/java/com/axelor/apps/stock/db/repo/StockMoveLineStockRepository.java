/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.util.Map;

public class StockMoveLineStockRepository extends StockMoveLineRepository {

  @Override
  public StockMoveLine copy(StockMoveLine entity, boolean deep) {
    StockMoveLine copy = super.copy(entity, deep);
    copy.setStockMove(null);
    copy.setPlannedStockMove(null);
    return copy;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long stockMoveLineId = (Long) json.get("id");
    StockMoveLine stockMoveLine = find(stockMoveLineId);

    StockMove stockMove = stockMoveLine.getStockMove();

    if (stockMove == null
        || stockMove.getStatusSelect() > StockMoveRepository.STATUS_PLANNED
        || (stockMove.getFromStockLocation() != null
            && stockMove.getFromStockLocation().getTypeSelect()
                == StockLocationRepository.TYPE_VIRTUAL)) {

      return super.populate(json, context);
    }

    Beans.get(StockMoveLineService.class)
        .updateAvailableQty(stockMoveLine, stockMoveLine.getStockMove().getFromStockLocation());

    if (stockMoveLine.getProduct() != null) {
      BigDecimal availableQty = stockMoveLine.getAvailableQty();
      BigDecimal availableQtyForProduct = stockMoveLine.getAvailableQtyForProduct();
      BigDecimal realQty = stockMoveLine.getRealQty();

      if (availableQty.compareTo(realQty) >= 0) {

        json.put("availableStatus", I18n.get("Available"));
      } else if (availableQtyForProduct.compareTo(realQty) >= 0) {

        json.put("availableStatus", I18n.get("Av. for product"));

      } else if (availableQty.compareTo(realQty) < 0
          && availableQtyForProduct.compareTo(realQty) < 0) {

        BigDecimal missingQty = BigDecimal.ZERO;
        if (stockMoveLine.getProduct().getTrackingNumberConfiguration() != null) {
          missingQty = availableQtyForProduct.subtract(realQty);
        } else {
          missingQty = availableQty.subtract(realQty);
        }
        json.put("availableStatus", I18n.get("Missing") + " (" + missingQty + ")");
      }
    }
    return super.populate(json, context);
  }
}
