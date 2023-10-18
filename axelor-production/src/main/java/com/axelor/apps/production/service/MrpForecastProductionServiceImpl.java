/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

  protected final ProductRepository productRepo;
  protected final MrpForecastRepository mrpForecastRepo;

  @Inject
  public MrpForecastProductionServiceImpl(
      ProductRepository productRepo, MrpForecastRepository mrpForecastRepo) {
    this.productRepo = productRepo;
    this.mrpForecastRepo = mrpForecastRepo;
  }

  @Override
  @SuppressWarnings("unchecked")
  @Transactional(rollbackOn = Exception.class)
  public void generateMrpForecast(
      Period period,
      List<LinkedHashMap<String, Object>> mrpForecastList,
      StockLocation stockLocation,
      int technicalOrigin) {

    for (LinkedHashMap<String, Object> mrpForecastItem : mrpForecastList) {
      Long id =
          mrpForecastItem.get("id") != null
              ? Long.parseLong(mrpForecastItem.get("id").toString())
              : null;
      BigDecimal qty = new BigDecimal(mrpForecastItem.get("qty").toString());
      LinkedHashMap<String, Object> productMap =
          (LinkedHashMap<String, Object>) mrpForecastItem.get("product");
      Product product = productRepo.find(Long.parseLong(productMap.get("id").toString()));
      LocalDate forecastDate = LocalDate.parse(mrpForecastItem.get("forecastDate").toString());
      if (id != null && qty.compareTo(BigDecimal.ZERO) == 0) {
        mrpForecastRepo.remove(mrpForecastRepo.find(id));
      } else if (qty.compareTo(BigDecimal.ZERO) != 0) {
        this.createOrUpdateMrpForecast(
            id, forecastDate, product, stockLocation, qty, technicalOrigin);
      }
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void createOrUpdateMrpForecast(
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
}
