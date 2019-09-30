/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.StockLocationSaveService;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.inject.Beans;
import java.util.Map;

public class StockLocationStockRepository extends StockLocationRepository {

  /**
   * Override to remove incompatible stock locations in partners
   *
   * @param entity
   * @return
   */
  @Override
  public StockLocation save(StockLocation entity) {
    Beans.get(StockLocationSaveService.class).removeForbiddenDefaultStockLocation(entity);
    return super.save(entity);
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long stocklocationId = (Long) json.get("id");
    StockLocation stockLocation = find(stocklocationId);

    if (stockLocation.getTypeSelect() == StockLocationRepository.TYPE_VIRTUAL) {
      return super.populate(json, context);
    }

    json.put(
        "stockLocationValue",
        Beans.get(StockLocationService.class).getStockLocationValue(stockLocation));

    return super.populate(json, context);
  }
}
