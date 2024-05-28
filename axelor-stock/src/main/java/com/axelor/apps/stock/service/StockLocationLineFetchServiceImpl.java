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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.utils.helpers.StringHelper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class StockLocationLineFetchServiceImpl implements StockLocationLineFetchService {

  @Override
  public BigDecimal getTrackingNumberAvailableQty(
      StockLocation stockLocation, TrackingNumber trackingNumber) {
    StockLocationLine detailStockLocationLine =
        getDetailLocationLine(stockLocation, trackingNumber.getProduct(), trackingNumber);

    BigDecimal availableQty = BigDecimal.ZERO;

    if (detailStockLocationLine != null) {
      availableQty = detailStockLocationLine.getCurrentQty();
    }
    return availableQty;
  }

  @Override
  public BigDecimal getTrackingNumberAvailableQty(TrackingNumber trackingNumber) {
    List<StockLocationLine> detailStockLocationLines =
        getDetailLocationLines(trackingNumber.getProduct(), trackingNumber);

    BigDecimal availableQty = BigDecimal.ZERO;

    if (detailStockLocationLines != null) {
      availableQty =
          detailStockLocationLines.stream()
              .map(StockLocationLine::getCurrentQty)
              .filter(Objects::nonNull)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    return availableQty;
  }

  @Override
  public StockLocationLine getDetailLocationLine(
      StockLocation stockLocation, Product product, TrackingNumber trackingNumber) {
    return JPA.all(StockLocationLine.class)
        .filter(
            "self.detailsStockLocation.id = :_stockLocationId "
                + "AND self.product.id = :_productId "
                + "AND self.trackingNumber.id = :_trackingNumberId")
        .bind("_stockLocationId", stockLocation.getId())
        .bind("_productId", product.getId())
        .bind("_trackingNumberId", trackingNumber.getId())
        .fetchOne();
  }

  @Override
  public List<StockLocationLine> getDetailLocationLines(
      Product product, TrackingNumber trackingNumber) {
    return JPA.all(StockLocationLine.class)
        .filter(
            "self.product.id = :_productId "
                + "AND self.trackingNumber.id = :_trackingNumberId "
                + "AND self.detailsStockLocation.typeSelect = :internalType")
        .bind("_productId", product.getId())
        .bind("_trackingNumberId", trackingNumber.getId())
        .bind("internalType", StockLocationRepository.TYPE_INTERNAL)
        .fetch();
  }

  @Override
  public BigDecimal getAvailableQty(StockLocation stockLocation, Product product) {
    StockLocationLine stockLocationLine = getStockLocationLine(stockLocation, product);
    BigDecimal availableQty = BigDecimal.ZERO;
    if (stockLocationLine != null) {
      availableQty = stockLocationLine.getCurrentQty();
    }
    return availableQty;
  }

  @Override
  public StockLocationLine getStockLocationLine(StockLocation stockLocation, Product product) {
    if (product == null || !product.getStockManaged() || stockLocation == null) {
      return null;
    }

    return JPA.all(StockLocationLine.class)
        .filter("self.stockLocation.id = :_stockLocationId " + "AND self.product.id = :_productId")
        .bind("_stockLocationId", stockLocation.getId())
        .bind("_productId", product.getId())
        .fetchOne();
  }

  @Override
  public List<StockLocationLine> getStockLocationLines(Product product) {
    if (product != null && !product.getStockManaged()) {
      return null;
    }

    return JPA.all(StockLocationLine.class)
        .filter("self.product.id = :_productId")
        .bind("_productId", product.getId())
        .fetch();
  }

  @Override
  public String getStockLocationLineListForAProduct(
      Long productId, Long companyId, Long stockLocationId) {

    String query =
        "self.product.id = "
            + productId
            + " AND self.stockLocation.typeSelect != "
            + StockLocationRepository.TYPE_VIRTUAL;

    if (companyId != 0L) {
      query += " AND self.stockLocation.company.id = " + companyId;
      if (stockLocationId != 0L) {
        StockLocation stockLocation = JPA.find(StockLocation.class, stockLocationId);
        List<StockLocation> stockLocationList =
            Beans.get(StockLocationService.class)
                .getAllLocationAndSubLocation(stockLocation, false);
        if (!stockLocationList.isEmpty() && stockLocation.getCompany().getId().equals(companyId)) {
          query +=
              " AND self.stockLocation.id IN ("
                  + StringHelper.getIdListString(stockLocationList)
                  + ") ";
        }
      }
    }
    return query;
  }

  @Override
  public String getAvailableStockForAProduct(Long productId, Long companyId, Long stockLocationId) {
    String query = this.getStockLocationLineListForAProduct(productId, companyId, stockLocationId);
    query +=
        " AND (self.currentQty != 0 OR self.futureQty != 0) "
            + " AND (self.stockLocation.isNotInCalculStock = false OR self.stockLocation.isNotInCalculStock IS NULL)";
    return query;
  }

  @Override
  public String getRequestedReservedQtyForAProduct(
      Long productId, Long companyId, Long stockLocationId) {
    String query = this.getStockLocationLineListForAProduct(productId, companyId, stockLocationId);
    query += " AND self.requestedReservedQty > 0";
    return query;
  }
}
