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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Site;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.DefaultStockLocationBySite;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.app.AppStockService;
import com.google.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StockMoveLineStockLocationServiceImpl implements StockMoveLineStockLocationService {

  protected AppBaseService appBaseService;
  protected AppStockService appStockService;
  protected StockLocationService stockLocationService;

  @Inject
  public StockMoveLineStockLocationServiceImpl(
      AppBaseService appBaseService,
      AppStockService appStockService,
      StockLocationService stockLocationService) {
    this.appBaseService = appBaseService;
    this.appStockService = appStockService;
    this.stockLocationService = stockLocationService;
  }

  @Override
  public void fillStockLocationWithDefaultStockLocation(
      StockMoveLine stockMoveLine, StockMove stockMove) {

    if (!appBaseService.getAppBase().getEnableSiteManagementForStock()
        || !appStockService.getAppStock().getIsManageStockLocationOnStockMoveLine()
        || stockMoveLine.getProduct() == null) {
      return;
    }
    Integer typeSelect = stockMove.getTypeSelect();

    // outgoing => fill from
    if (typeSelect == StockMoveRepository.TYPE_OUTGOING) {
      StockLocation defaultFromStockLocation =
          getDefaultFromStockLocation(stockMoveLine.getProduct(), stockMove);
      if (defaultFromStockLocation != null) {
        stockMoveLine.setFromStockLocation(defaultFromStockLocation);
      }

      // incoming => fill to
    } else if (typeSelect == StockMoveRepository.TYPE_INCOMING) {
      StockLocation defaultToStockLocation =
          getDefaultToStockLocation(stockMoveLine.getProduct(), stockMove);
      if (defaultToStockLocation != null) {
        stockMoveLine.setToStockLocation(defaultToStockLocation);
      }
    }
  }

  @Override
  public StockLocation getDefaultFromStockLocation(Product product, StockMove stockMove) {

    StockLocation fromStockLocation = null;
    List<DefaultStockLocationBySite> defaultStockLocationBySiteList =
        product.getDefaultStockLocationBySite();

    if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING) {

      if (stockMove.getIsReversion()) {
        fromStockLocation =
            getDefaultStockLocation(
                defaultStockLocationBySiteList,
                stockMove.getFromStockLocation(),
                DefaultStockLocationBySite::getReceiptDefaultStockLocation);
      } else {
        fromStockLocation =
            getDefaultStockLocation(
                defaultStockLocationBySiteList,
                stockMove.getFromStockLocation(),
                DefaultStockLocationBySite::getPickupDefaultStockLocation);
      }
    }
    return fromStockLocation;
  }

  @Override
  public StockLocation getDefaultToStockLocation(Product product, StockMove stockMove) {
    StockLocation toStockLocation = null;

    List<DefaultStockLocationBySite> defaultStockLocationBySiteList =
        product.getDefaultStockLocationBySite();

    if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING) {

      if (stockMove.getIsReversion()) {
        toStockLocation =
            getDefaultStockLocation(
                defaultStockLocationBySiteList,
                stockMove.getToStockLocation(),
                DefaultStockLocationBySite::getPickupDefaultStockLocation);
      } else {
        toStockLocation =
            getDefaultStockLocation(
                defaultStockLocationBySiteList,
                stockMove.getToStockLocation(),
                DefaultStockLocationBySite::getReceiptDefaultStockLocation);
      }
    }
    return toStockLocation;
  }

  protected StockLocation getDefaultStockLocation(
      List<DefaultStockLocationBySite> defaultStockLocationBySiteList,
      StockLocation stockMoveStockLocation,
      Function<DefaultStockLocationBySite, StockLocation> mapDefaultStockLocation) {
    Site stockLocationSite = stockMoveStockLocation.getSite();

    return defaultStockLocationBySiteList.stream()
        .filter(defStockLocation -> defStockLocation.getSite().equals(stockLocationSite))
        .findFirst()
        .map(mapDefaultStockLocation)
        .orElse(stockMoveStockLocation);
  }

  @Override
  public String getStockLocationDomainWithDefaultLocation(
      StockMoveLine stockMoveLine, StockMove stockMove, StockLocation stockLocation) {
    if (stockLocation == null) {
      return "self.id in (0)";
    }

    List<StockLocation> stockLocationList =
        stockLocationService.getAllLocationAndSubLocation(stockLocation, false);

    // add default stockLocation if options are enabled
    if (appStockService.getAppStock().getIsManageStockLocationOnStockMoveLine()
        && appBaseService.getAppBase().getEnableSiteManagementForStock()
        && stockMoveLine.getProduct() != null) {

      StockLocation stockLocationForMoveLine = null;

      Integer typeSelect = stockMove.getTypeSelect();
      if (typeSelect == StockMoveRepository.TYPE_INCOMING) {
        stockLocationForMoveLine = getDefaultToStockLocation(stockMoveLine.getProduct(), stockMove);
      } else if (typeSelect == StockMoveRepository.TYPE_OUTGOING) {
        stockLocationForMoveLine =
            getDefaultFromStockLocation(stockMoveLine.getProduct(), stockMove);
      }

      if (stockLocationForMoveLine != null) {
        stockLocationList.add(stockLocationForMoveLine);
      }
    }

    return String.format(
        "self.id in (%s)",
        stockLocationList.stream()
            .map(location -> location.getId().toString())
            .collect(Collectors.joining(",")));
  }
}
