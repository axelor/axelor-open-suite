/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.TrackingNumberConfigurationService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.Map;

public class StockMoveLineStockRepository extends StockMoveLineRepository {

  protected TrackingNumberConfigurationService trackingNumberConfigurationService;

  @Inject
  public StockMoveLineStockRepository(
      TrackingNumberConfigurationService trackingNumberConfigurationService) {
    this.trackingNumberConfigurationService = trackingNumberConfigurationService;
  }

  @Override
  public StockMoveLine copy(StockMoveLine entity, boolean deep) {
    StockMoveLine copy = super.copy(entity, deep);
    copy.setStockMove(null);
    copy.setPlannedStockMove(null);
    return copy;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      Long stockMoveLineId = (Long) json.get("id");
      StockMoveLine stockMoveLine = find(stockMoveLineId);

      StockMove stockMove = stockMoveLine.getStockMove();

      populateTrackingNumberConfig(json, stockMoveLine);
      if (stockMove == null
          || (stockMoveLine.getFromStockLocation() != null
              && stockMoveLine.getFromStockLocation().getTypeSelect()
                  == StockLocationRepository.TYPE_VIRTUAL)) {

        return super.populate(json, context);
      }
      if (stockMove.getStatusSelect() < StockMoveRepository.STATUS_REALIZED) {
        Beans.get(StockMoveLineService.class).setAvailableStatus(stockMoveLine);
        json.put(
            "availableStatus",
            stockMoveLine.getProduct() != null && stockMoveLine.getProduct().getStockManaged()
                ? stockMoveLine.getAvailableStatus()
                : null);
        json.put("availableStatusSelect", stockMoveLine.getAvailableStatusSelect());
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return super.populate(json, context);
  }

  protected void populateTrackingNumberConfig(Map<String, Object> json, StockMoveLine stockMoveLine)
      throws AxelorException {
    final String trackingNumberConfiguration = "$trackingNumberConfiguration";
    json.put(
        trackingNumberConfiguration,
        trackingNumberConfigurationService.getTrackingNumberConfiguration(
            stockMoveLine, stockMoveLine.getStockMove()));
  }
}
