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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.meta.CallMethod;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StockLocationService {

  @CallMethod
  BigDecimal getRealQtyOfProductInStockLocations(
      Long productId, List<Long> stockLocationIds, Long companyId) throws AxelorException;

  @CallMethod
  BigDecimal getFutureQtyOfProductInStockLocations(
      Long productId, List<Long> stockLocationIds, Long companyId) throws AxelorException;

  public Map<String, Object> getStockIndicators(Long productId, Long companyId, Long locationId)
      throws AxelorException;

  @CallMethod
  public List<Long> getBadStockLocationLineId();

  @CallMethod
  public Set<Long> getContentStockLocationIds(StockLocation stockLocation);

  public List<StockLocation> getAllLocationAndSubLocation(
      StockLocation stockLocation, boolean isVirtualInclude);

  public BigDecimal getStockLocationValue(StockLocation stockLocation);

  public List<Long> getAllLocationAndSubLocationId(
      StockLocation stockLocation, boolean isVirtualInclude);

  public boolean isConfigMissing(StockLocation stockLocation, int printType);

  void changeProductLocker(StockLocation stockLocation, Product product, String newLocker)
      throws AxelorException;
}
