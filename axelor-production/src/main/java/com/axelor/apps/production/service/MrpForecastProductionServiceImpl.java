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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.supplychain.db.MrpForecast;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

public class MrpForecastProductionServiceImpl implements MrpForecastProductionService {

  @Inject ProductRepository productRepo;
  @Inject MrpForecastRepository mrpForecastRepo;

  @Override
  public void generateMrpForecast(
      Period period,
      List<LinkedHashMap<String, Object>> mrpForecastList,
      StockLocation stockLocation,
      int technicalOrigin) {
    LocalDate forecastDate = period.getToDate();

    for (LinkedHashMap<String, Object> mrpForecastItem : mrpForecastList) {
      Long id =
          mrpForecastItem.get("id") != null
              ? Long.parseLong(mrpForecastItem.get("id").toString())
              : null;
      BigDecimal qty = new BigDecimal(mrpForecastItem.get("qty").toString());
      @SuppressWarnings("unchecked")
      LinkedHashMap<String, Object> productMap =
          (LinkedHashMap<String, Object>) mrpForecastItem.get("product");
      Product product = productRepo.find(Long.parseLong(productMap.get("id").toString()));
      if (id != null && qty.compareTo(BigDecimal.ZERO) == 0) {
        this.RemoveMrpForecast(id);
      } else if (qty.compareTo(BigDecimal.ZERO) != 0) {
        this.createMrpForecast(id, forecastDate, product, stockLocation, qty, technicalOrigin);
      }
    }
  }

  @Transactional
  public void createMrpForecast(
      Long id,
      LocalDate forecastDate,
      Product product,
      StockLocation stockLocation,
      BigDecimal qty,
      int technicalOrigin) {
    Unit unit = product.getSalesUnit() != null ? product.getSalesUnit() : product.getUnit();
    MrpForecast mrpForecast = id != null ? mrpForecastRepo.find(id) : new MrpForecast();
    if (id != null
        && mrpForecast.getForecastDate().equals(forecastDate)
        && mrpForecast.getStockLocation().equals(stockLocation)
        && mrpForecast.getQty().compareTo(qty) == 0
        && mrpForecast.getUnit().equals(unit)) {
      return;
    }
    mrpForecast.setForecastDate(forecastDate);
    mrpForecast.setProduct(product);
    mrpForecast.setStockLocation(stockLocation);
    mrpForecast.setQty(qty);
    mrpForecast.setTechnicalOrigin(technicalOrigin);
    mrpForecast.setUnit(unit);
    mrpForecast.setStatusSelect(MrpForecastRepository.STATUS_DRAFT);
    mrpForecastRepo.save(mrpForecast);
  }

  @Transactional
  public void RemoveMrpForecast(Long id) {
    MrpForecast mrpForecast = id != null ? mrpForecastRepo.find(id) : new MrpForecast();
    mrpForecastRepo.remove(mrpForecast);
  }
}
