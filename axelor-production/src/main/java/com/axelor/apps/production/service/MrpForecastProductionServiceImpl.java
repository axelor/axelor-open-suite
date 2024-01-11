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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.supplychain.db.MrpForecast;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.db.mapper.Mapper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class MrpForecastProductionServiceImpl implements MrpForecastProductionService {

  protected final ProductRepository productRepo;
  protected final MrpForecastRepository mrpForecastRepo;
  protected final ProductCompanyService productCompanyService;

  @Inject
  public MrpForecastProductionServiceImpl(
      ProductRepository productRepo,
      MrpForecastRepository mrpForecastRepo,
      ProductCompanyService productCompanyService) {
    this.productRepo = productRepo;
    this.mrpForecastRepo = mrpForecastRepo;
    this.productCompanyService = productCompanyService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void generateMrpForecast(
      Period period,
      List<MrpForecast> mrpForecastList,
      StockLocation stockLocation,
      int technicalOrigin) {

    for (MrpForecast mrpForecast : mrpForecastList) {
      Long id = mrpForecast.getId();
      BigDecimal qty = mrpForecast.getQty();
      LocalDate forecastDate = mrpForecast.getForecastDate();
      if (id != null && qty.compareTo(BigDecimal.ZERO) == 0) {
        mrpForecastRepo.remove(mrpForecastRepo.find(id));
      } else if (qty.compareTo(BigDecimal.ZERO) != 0) {
        Product product = productRepo.find(mrpForecast.getProduct().getId());
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

  @Override
  public BigDecimal computeTotalForecast(List<MrpForecast> mrpForecastList, Company company)
      throws AxelorException {
    BigDecimal totalForecast = BigDecimal.ZERO;
    if (CollectionUtils.isNotEmpty(mrpForecastList)) {
      for (MrpForecast mrpForecast : mrpForecastList) {
        BigDecimal qty = mrpForecast.getQty();
        if (qty.signum() == 0) {
          continue;
        }
        totalForecast =
            totalForecast.add(qty.multiply(getSalePrice(mrpForecast.getProduct(), company)));
      }
    }
    return totalForecast;
  }

  @Override
  public List<Map<String, Object>> resetMrpForecasts(
      List<MrpForecast> mrpForecastList, Company company) throws AxelorException {
    List<Map<String, Object>> data = new ArrayList<>();
    for (MrpForecast mrpForecast : mrpForecastList) {
      Map<String, Object> map = Mapper.toMap(mrpForecast);
      map.put("qty", BigDecimal.ZERO);
      map.put("$totalPrice", BigDecimal.ZERO);
      map.put("$unitPrice", getSalePrice(mrpForecast.getProduct(), company));
      data.add(map);
    }
    return data;
  }

  protected BigDecimal getSalePrice(Product product, Company company) throws AxelorException {
    return (BigDecimal) productCompanyService.get(product, "salePrice", company);
  }
}
