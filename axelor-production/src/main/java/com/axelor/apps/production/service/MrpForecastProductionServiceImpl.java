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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Product;
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
      StockLocation stockLocation) {
    LocalDate forecastDate = period.getToDate();

    for (LinkedHashMap<String, Object> mrpForecastItem : mrpForecastList) {
      BigDecimal qty = new BigDecimal(mrpForecastItem.get("qty").toString());
      if (qty.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      @SuppressWarnings("unchecked")
      LinkedHashMap<String, Object> productMap =
          (LinkedHashMap<String, Object>) mrpForecastItem.get("product");
      Product product = productRepo.find(Long.parseLong(productMap.get("id").toString()));
      this.createMrpForecast(forecastDate, product, stockLocation, qty);
    }
  }

  @Transactional
  public void createMrpForecast(
      LocalDate forecastDate, Product product, StockLocation stockLocation, BigDecimal qty) {
    MrpForecast mrpForecast = new MrpForecast();
    mrpForecast.setForecastDate(forecastDate);
    mrpForecast.setProduct(product);
    mrpForecast.setStockLocation(stockLocation);
    mrpForecast.setQty(qty);
    mrpForecastRepo.save(mrpForecast);
  }
}
