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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.common.StringUtils;
import com.axelor.utils.helpers.StringHelper;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManufOrderStockLocationQueryServiceImpl
    implements ManufOrderStockLocationQueryService {

  protected AppProductionService appProductionService;
  protected StockLocationRepository stockLocationRepository;
  protected StockLocationService stockLocationService;

  @Inject
  public ManufOrderStockLocationQueryServiceImpl(
      AppProductionService appProductionService,
      StockLocationRepository stockLocationRepository,
      StockLocationService stockLocationService) {
    this.appProductionService = appProductionService;
    this.stockLocationRepository = stockLocationRepository;
    this.stockLocationService = stockLocationService;
  }

  @Override
  public String getConsumeAndMissingQtyForAProduct(
      Long productId, Long companyId, Long stockLocationId) {
    List<Integer> statusList = getMOFiltersOnProductionConfig();
    String statusListQuery =
        statusList.stream().map(String::valueOf).collect(Collectors.joining(","));
    String query =
        "self.product.id = "
            + productId
            + " AND self.stockMove.statusSelect = "
            + StockMoveRepository.STATUS_PLANNED
            + " AND self.fromStockLocation.typeSelect != "
            + StockLocationRepository.TYPE_VIRTUAL
            + " AND ( (self.consumedManufOrder IS NOT NULL AND self.consumedManufOrder.statusSelect IN ("
            + statusListQuery
            + "))"
            + " OR (self.consumedOperationOrder IS NOT NULL AND self.consumedOperationOrder.statusSelect IN ( "
            + statusListQuery
            + ") ) ) ";
    if (companyId != 0L) {
      query += " AND self.stockMove.company.id = " + companyId;
      if (stockLocationId != 0L) {
        StockLocation stockLocation = stockLocationRepository.find(stockLocationId);
        List<StockLocation> stockLocationList =
            stockLocationService.getAllLocationAndSubLocation(stockLocation, false);
        if (!stockLocationList.isEmpty() && stockLocation.getCompany().getId().equals(companyId)) {
          query +=
              " AND self.fromStockLocation.id IN ("
                  + StringHelper.getIdListString(stockLocationList)
                  + ") ";
        }
      }
    }

    return query;
  }

  @Override
  public String getBuildingQtyForAProduct(Long productId, Long companyId, Long stockLocationId) {
    List<Integer> statusList = getMOFiltersOnProductionConfig();
    String statusListQuery =
        statusList.stream().map(String::valueOf).collect(Collectors.joining(","));
    String query =
        "self.product.id = "
            + productId
            + " AND self.stockMove.statusSelect = "
            + StockMoveRepository.STATUS_PLANNED
            + " AND self.stockMove.toStockLocation.typeSelect != "
            + StockLocationRepository.TYPE_VIRTUAL
            + " AND self.producedManufOrder IS NOT NULL "
            + " AND self.producedManufOrder.statusSelect IN ( "
            + statusListQuery
            + " )";
    if (companyId != 0L) {
      query += "AND self.stockMove.company.id = " + companyId;
      if (stockLocationId != 0L) {
        StockLocation stockLocation = stockLocationRepository.find(stockLocationId);
        List<StockLocation> stockLocationList =
            stockLocationService.getAllLocationAndSubLocation(stockLocation, false);
        if (!stockLocationList.isEmpty() && stockLocation.getCompany().getId().equals(companyId)) {
          query +=
              " AND self.stockMove.toStockLocation.id IN ("
                  + StringHelper.getIdListString(stockLocationList)
                  + ") ";
        }
      }
    }

    return query;
  }

  private List<Integer> getMOFiltersOnProductionConfig() {
    List<Integer> statusList = new ArrayList<>();
    statusList.add(ManufOrderRepository.STATUS_IN_PROGRESS);
    statusList.add(ManufOrderRepository.STATUS_STANDBY);
    String status = appProductionService.getAppProduction().getmOFilterOnStockDetailStatusSelect();
    if (!StringUtils.isBlank(status)) {
      statusList = StringHelper.getIntegerList(status);
    }
    return statusList;
  }
}
