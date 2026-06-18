/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import java.math.BigDecimal;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public interface ProductionTrackingPreservationService {

  record PreservedTrackingNumber(
      TrackingNumber trackingNumber, BigDecimal qty, Unit unit, boolean restoreOrigin) {}

  record PreservedTrackingNumbersByProduct(Map<Long, Deque<PreservedTrackingNumber>> values) {

    public Deque<PreservedTrackingNumber> getByProductId(Long productId) {
      return values != null ? values.get(productId) : null;
    }
  }

  /** Preserve tracking numbers from all given stock move lines (full snapshot). */
  PreservedTrackingNumbersByProduct getPreservedTrackingNumbersByProduct(
      List<StockMoveLine> stockMoveLines);

  /** Preserve tracking numbers only from lines removed between original and current lists. */
  PreservedTrackingNumbersByProduct getPreservedTrackingNumbersByProduct(
      List<StockMoveLine> originalStockMoveLines, List<StockMoveLine> currentStockMoveLines);

  /** Create a single stock move line with a specific preserved tracking number. */
  StockMoveLine createStockMoveLineWithPreservedTracking(
      ProdProduct prodProduct,
      StockMove stockMove,
      int inOrOutType,
      BigDecimal qty,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      TrackingNumber trackingNumber,
      boolean restoreOrigin)
      throws AxelorException;

  /**
   * Create stock move lines for the given qty, consuming preserved tracking numbers first. Falls
   * back to auto-generation only when preservation is exhausted.
   */
  void createStockMoveLinesWithPreservedTracking(
      ProdProduct prodProduct,
      StockMove stockMove,
      int inOrOutType,
      BigDecimal qty,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      PreservedTrackingNumbersByProduct preservedTrackingNumbersByProduct)
      throws AxelorException;

  /**
   * Drain all remaining entries from the preservation deques into stock move lines. Used to create
   * reserve lines that carry forward unused tracking to the next partial finish cycle.
   */
  void drainRemainingPreservedTracking(
      List<ProdProduct> prodProductList,
      StockMove stockMove,
      int inOrOutType,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      PreservedTrackingNumbersByProduct preservedTrackingNumbersByProduct)
      throws AxelorException;
}
