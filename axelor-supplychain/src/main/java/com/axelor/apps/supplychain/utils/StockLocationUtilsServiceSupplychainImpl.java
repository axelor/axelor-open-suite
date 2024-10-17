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
package com.axelor.apps.supplychain.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.utils.StockLocationUtilsServiceImpl;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class StockLocationUtilsServiceSupplychainImpl extends StockLocationUtilsServiceImpl
    implements StockLocationUtilsServiceSupplychain {

  @Inject
  public StockLocationUtilsServiceSupplychainImpl(
      UnitRepository unitRepository,
      UnitConversionService unitConversionService,
      AppBaseService appBaseService) {
    super(unitRepository, unitConversionService, appBaseService);
  }

  @Override
  public BigDecimal getReservedQtyOfProductInStockLocations(
      Long productId, List<Long> stockLocationIds, Long companyId) throws AxelorException {
    return getQtyOfProductInStockLocations(productId, stockLocationIds, companyId, "reservedQty");
  }
}
