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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.exception.AxelorException;
import com.axelor.meta.CallMethod;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface StockLocationService {

  /**
   * Get default receipt location for the given company.
   *
   * @param company
   * @return the default stock location if found, null if there was an exception or if the default
   *     location is empty
   */
  StockLocation getDefaultReceiptStockLocation(Company company);

  /**
   * Get default pickup location for the given company.
   *
   * @param company
   * @return the default stock location if found, null if there was an exception or if the default
   *     location is empty
   */
  StockLocation getPickupDefaultStockLocation(Company company);

  public BigDecimal getQty(Long productId, Long locationId, Long companyId, String qtyType)
      throws AxelorException;

  @CallMethod
  public BigDecimal getRealQty(Long productId, Long locationId, Long companyId)
      throws AxelorException;

  @CallMethod
  public BigDecimal getFutureQty(Long productId, Long locationId, Long companyId)
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
}
