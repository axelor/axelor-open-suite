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
package com.axelor.apps.production.service.config;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class StockConfigProductionService extends StockConfigService {

  protected AppProductionService appProductionService;

  @Inject
  public StockConfigProductionService(AppProductionService appProductionService) {
    this.appProductionService = appProductionService;
  }

  public StockLocation getProductionVirtualStockLocation(
      StockConfig stockConfig, boolean isOutsource) throws AxelorException {

    if (stockConfig.getProductionVirtualStockLocation() == null) {
      throw new AxelorException(
          stockConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.PRODUCTION_CONFIG_2),
          stockConfig.getCompany().getName());
    }

    if (isOutsource
        && !stockConfig.getProductionVirtualStockLocation().getIsOutsourcingLocation()) {
      throw new AxelorException(
          stockConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.PRODUCTION_CONFIG_STOCK_LOCATION_NOT_OUTSOURCING));
    }

    return stockConfig.getProductionVirtualStockLocation();
  }

  public StockLocation getWasteStockLocation(StockConfig stockConfig) throws AxelorException {
    if (stockConfig.getWasteStockLocation() == null) {
      throw new AxelorException(
          stockConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.PRODUCTION_CONFIG_3),
          stockConfig.getCompany().getName());
    }
    return stockConfig.getWasteStockLocation();
  }

  public StockLocation getFinishedProductsDefaultStockLocation(
      StockLocation workshop, StockConfig stockConfig) throws AxelorException {
    StockLocation finishedProductsDefaultStockLocation = null;
    if (appProductionService.getAppBase().getEnableTradingNamesManagement()
        && workshop != null
        && workshop.getTradingName() != null) {
      finishedProductsDefaultStockLocation =
          workshop.getTradingName().getFinishedProductsDefaultStockLocation();
    }
    if (finishedProductsDefaultStockLocation == null) {
      finishedProductsDefaultStockLocation = stockConfig.getFinishedProductsDefaultStockLocation();
    }
    if (finishedProductsDefaultStockLocation == null) {
      throw new AxelorException(
          stockConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.PRODUCTION_CONFIG_4),
          stockConfig.getCompany().getName());
    }
    return finishedProductsDefaultStockLocation;
  }

  public StockLocation getComponentDefaultStockLocation(
      StockLocation workshop, StockConfig stockConfig) throws AxelorException {
    StockLocation componentDefaultStockLocation = null;
    if (appProductionService.getAppBase().getEnableTradingNamesManagement()
        && workshop != null
        && workshop.getTradingName() != null) {
      componentDefaultStockLocation = workshop.getTradingName().getComponentDefaultStockLocation();
    }
    if (componentDefaultStockLocation == null) {
      componentDefaultStockLocation = stockConfig.getComponentDefaultStockLocation();
    }
    if (componentDefaultStockLocation == null) {
      throw new AxelorException(
          stockConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.PRODUCTION_CONFIG_5),
          stockConfig.getCompany().getName());
    }
    return componentDefaultStockLocation;
  }
}
