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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.TrackingNumber;
import java.math.BigDecimal;
import java.util.List;

public interface StockLocationLineFetchService {

  /**
   * Allow to get the available qty of product for a given Tracking Number.
   *
   * @param stockLocation
   * @param trackingNumber
   * @return
   */
  BigDecimal getTrackingNumberAvailableQty(
      StockLocation stockLocation, TrackingNumber trackingNumber);

  BigDecimal getTrackingNumberAvailableQty(TrackingNumber trackingNumber);

  /**
   * Allow to get the detailed location line of a given product, product variant and tracking number
   * in a given location.
   *
   * @param stockLocation A location
   * @param product A product
   * @param trackingNumber A tracking number
   * @return The stock location line if found, else null
   */
  StockLocationLine getDetailLocationLine(
      StockLocation stockLocation, Product product, TrackingNumber trackingNumber);

  List<StockLocationLine> getDetailLocationLines(Product product, TrackingNumber trackingNumber);

  /**
   * Allow to get the available qty of product in a given location.
   *
   * @param stockLocation
   * @param product
   * @return
   */
  BigDecimal getAvailableQty(StockLocation stockLocation, Product product);

  /**
   * Allow to get the location line of a given product in a given location.
   *
   * @param stockLocation A location
   * @param product A product
   * @return The stock location line if found, else null
   */
  StockLocationLine getStockLocationLine(StockLocation stockLocation, Product product);

  /**
   * Allow to get the location lines of a given product.
   *
   * @param product
   * @return
   */
  List<StockLocationLine> getStockLocationLines(Product product);

  /**
   * Create a query to find stock location line of a product of a specific/all company and a
   * specific/all stock location
   *
   * @param productId, companyId and stockLocationId
   * @return the query.
   */
  String getStockLocationLineListForAProduct(Long productId, Long companyId, Long stockLocationId);

  /**
   * Create a query to find product's available qty of a specific/all company and a specific/all
   * stock location
   *
   * @param productId, companyId and stockLocationId
   * @return the query.
   */
  String getAvailableStockForAProduct(Long productId, Long companyId, Long stockLocationId);

  /**
   * Create a query to find product's requested reserved qty of a specific/all company and a
   * specific/all stock location
   *
   * @param productId, companyId and stockLocationId
   * @return the query.
   */
  String getRequestedReservedQtyForAProduct(Long productId, Long companyId, Long stockLocationId);
}
