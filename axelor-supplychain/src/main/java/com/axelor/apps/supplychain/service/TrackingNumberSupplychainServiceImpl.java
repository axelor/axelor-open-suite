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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;

public class TrackingNumberSupplychainServiceImpl implements TrackingNumberSupplychainService {

  protected final TrackingNumberRepository trackingNumberRepository;

  @Inject
  public TrackingNumberSupplychainServiceImpl(TrackingNumberRepository trackingNumberRepository) {
    this.trackingNumberRepository = trackingNumberRepository;
  }

  @Override
  public void freeOriginSaleOrderLine(SaleOrderLine saleOrderLine) {
    trackingNumberRepository
        .all()
        .filter("self.originSaleOrderLine = :saleOrderLine")
        .bind("saleOrderLine", saleOrderLine)
        .fetch()
        .forEach(this::freeOriginSaleOrderLine);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void freeOriginSaleOrderLine(TrackingNumber trackingNumber) {
    Objects.requireNonNull(trackingNumber);

    trackingNumber.setOriginSaleOrderLine(null);
  }
}
