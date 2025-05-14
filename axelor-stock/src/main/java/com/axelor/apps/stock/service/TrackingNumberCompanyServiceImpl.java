/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class TrackingNumberCompanyServiceImpl implements TrackingNumberCompanyService {

  protected final StockMoveLineRepository stockMoveLineRepository;

  @Inject
  public TrackingNumberCompanyServiceImpl(StockMoveLineRepository stockMoveLineRepository) {
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  @Override
  public Optional<Company> getCompany(TrackingNumber trackingNumber) throws AxelorException {

    Objects.requireNonNull(trackingNumber);

    switch (trackingNumber.getOriginMoveTypeSelect()) {
      case TrackingNumberRepository.ORIGIN_MOVE_TYPE_MANUAL:
        return Optional.ofNullable(trackingNumber.getOriginStockMoveLine())
            .map(this::getStockMoveLine)
            .map(StockMoveLine::getStockMove)
            .map(StockMove::getCompany)
            .or(
                () ->
                    Optional.ofNullable(trackingNumber.getCreatedBy()).map(User::getActiveCompany));
      case TrackingNumberRepository.ORIGIN_MOVE_TYPE_INVENTORY:
        return Optional.ofNullable(trackingNumber.getOriginStockMoveLine())
            .map(this::getStockMoveLine)
            .map(StockMoveLine::getStockMove)
            .map(StockMove::getCompany)
            .or(
                () ->
                    Optional.ofNullable(trackingNumber.getOriginInventoryLine())
                        .map(InventoryLine::getInventory)
                        .map(Inventory::getCompany));
      default:
        return getDefaultCompany(trackingNumber);
    }
  }

  protected Optional<Company> getDefaultCompany(TrackingNumber trackingNumber)
      throws AxelorException {
    return Optional.empty();
  }

  protected StockMoveLine getStockMoveLine(StockMoveLine stockMoveLine) {
    if (stockMoveLine.getId() != null) {
      return stockMoveLineRepository.find(stockMoveLine.getId());
    }
    return stockMoveLine;
  }
}
