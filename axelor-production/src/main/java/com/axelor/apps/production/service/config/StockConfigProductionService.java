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
package com.axelor.apps.production.service.config;

import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;

public class StockConfigProductionService extends StockConfigService {

  public StockLocation getProductionVirtualStockLocation(
      StockConfig stockConfig, boolean isOutsource) throws AxelorException {

    if (stockConfig.getProductionVirtualStockLocation() == null
        || (isOutsource
            && !stockConfig.getProductionVirtualStockLocation().getIsOutsourcingLocation())) {
      throw new AxelorException(
          stockConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PRODUCTION_CONFIG_2),
          stockConfig.getCompany().getName());
    }

    return stockConfig.getProductionVirtualStockLocation();
  }

  public StockLocation getWasteStockLocation(StockConfig stockConfig) throws AxelorException {
    if (stockConfig.getWasteStockLocation() == null) {
      throw new AxelorException(
          stockConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PRODUCTION_CONFIG_3),
          stockConfig.getCompany().getName());
    }
    return stockConfig.getWasteStockLocation();
  }

  public StockLocation getFinishedProductsDefaultStockLocation(StockConfig stockConfig)
      throws AxelorException {
    if (stockConfig.getFinishedProductsDefaultStockLocation() == null) {
      throw new AxelorException(
          stockConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PRODUCTION_CONFIG_4),
          stockConfig.getCompany().getName());
    }
    return stockConfig.getFinishedProductsDefaultStockLocation();
  }

  public StockLocation getComponentDefaultStockLocation(StockConfig stockConfig)
      throws AxelorException {
    if (stockConfig.getComponentDefaultStockLocation() == null) {
      throw new AxelorException(
          stockConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PRODUCTION_CONFIG_5),
          stockConfig.getCompany().getName());
    }
    return stockConfig.getComponentDefaultStockLocation();
  }
}
