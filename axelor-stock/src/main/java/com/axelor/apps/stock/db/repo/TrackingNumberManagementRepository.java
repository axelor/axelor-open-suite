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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;

public class TrackingNumberManagementRepository extends TrackingNumberRepository {

  @Inject private StockLocationRepository stockLocationRepo;

  @Inject private StockLocationLineService stockLocationLineService;

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      Long trackingNumberId = (Long) json.get("id");
      TrackingNumber trackingNumber = find(trackingNumberId);

      if (trackingNumber.getProduct() != null && context.get("_parent") != null) {
        Map<String, Object> _parent = (Map<String, Object>) context.get("_parent");

        if (_parent.get("fromStockLocation") != null) {
          StockLocation stockLocation =
              stockLocationRepo.find(
                  Long.parseLong(((Map) _parent.get("fromStockLocation")).get("id").toString()));

          if (stockLocation != null) {
            BigDecimal availableQty =
                stockLocationLineService.getTrackingNumberAvailableQty(
                    stockLocation, trackingNumber);

            json.put("$availableQty", availableQty);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return super.populate(json, context);
  }
}
