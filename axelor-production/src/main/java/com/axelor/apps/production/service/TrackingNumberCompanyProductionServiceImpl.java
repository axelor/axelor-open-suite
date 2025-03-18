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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.supplychain.service.TrackingNumberCompanySupplychainServiceImpl;
import com.google.inject.Inject;
import java.util.Optional;

public class TrackingNumberCompanyProductionServiceImpl
    extends TrackingNumberCompanySupplychainServiceImpl {

  @Inject
  public TrackingNumberCompanyProductionServiceImpl(
      StockMoveLineRepository stockMoveLineRepository) {
    super(stockMoveLineRepository);
  }

  @Override
  protected Optional<Company> getDefaultCompany(TrackingNumber trackingNumber)
      throws AxelorException {

    if (trackingNumber.getOriginMoveTypeSelect()
        == TrackingNumberRepository.ORIGIN_MOVE_TYPE_MANUFACTURING) {
      return Optional.ofNullable(trackingNumber.getOriginStockMoveLine())
          .map(this::getStockMoveLine)
          .map(StockMoveLine::getStockMove)
          .map(StockMove::getCompany)
          .or(
              () ->
                  Optional.ofNullable(trackingNumber.getOriginManufOrder())
                      .map(ManufOrder::getCompany)
                      .or(
                          () ->
                              Optional.ofNullable(trackingNumber.getOriginStockMoveLine())
                                  .map(StockMoveLine::getStockMove)
                                  .map(StockMove::getCompany)));
    }
    return super.getDefaultCompany(trackingNumber);
  }
}
