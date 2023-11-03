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
package com.axelor.apps.production.web;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.supplychain.db.MrpForecast;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

@Singleton
public class SopLineController {

  @Inject MrpForecastRepository mrpForecastRepo;
  @Inject CurrencyRepository currencyRepo;

  @SuppressWarnings("unchecked")
  public void fillMrpForecast(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    LinkedHashMap<String, Object> productCategoryMap =
        (LinkedHashMap<String, Object>) context.get("_productCategory");
    LinkedHashMap<String, Object> sopLineMap =
        (LinkedHashMap<String, Object>) context.get("_sopLine");
    LinkedHashMap<String, Object> currencyMap =
        (LinkedHashMap<String, Object>) sopLineMap.get("currency");
    LinkedHashMap<String, Object> periodMap =
        (LinkedHashMap<String, Object>) sopLineMap.get("period");

    BigDecimal sopSalesForecast = new BigDecimal(sopLineMap.get("sopSalesForecast").toString());
    Period period =
        Beans.get(PeriodRepository.class).find(Long.parseLong(periodMap.get("id").toString()));
    Long productCategoryId = Long.parseLong(productCategoryMap.get("id").toString());
    Currency currency = currencyRepo.find(Long.parseLong(currencyMap.get("id").toString()));
    BigDecimal totalForecast = BigDecimal.ZERO;
    SortedSet<Map<String, Object>> mrpForecastSet =
        new TreeSet<Map<String, Object>>(Comparator.comparing(m -> (String) m.get("code")));
    List<Product> productList =
        Beans.get(ProductRepository.class)
            .all()
            .filter("self.productCategory.id = ?1 ", productCategoryId)
            .fetch();
    if (productList != null) {
      for (Product product : productList) {
        Map<String, Object> map = new HashMap<String, Object>();
        MrpForecast mrpForecast =
            mrpForecastRepo
                .all()
                .filter(
                    "self.product.id = ?1 AND self.technicalOrigin = ?2 AND self.forecastDate >= ?3 AND self.forecastDate <= ?4",
                    product.getId(),
                    MrpForecastRepository.TECHNICAL_ORIGIN_CREATED_FROM_SOP,
                    period.getFromDate(),
                    period.getToDate())
                .fetchOne();
        if (mrpForecast != null) {
          map = Mapper.toMap(mrpForecast);
          BigDecimal totalPrice = mrpForecast.getQty().multiply(product.getSalePrice());
          map.put("$totalPrice", totalPrice);
          map.put("$unitPrice", product.getSalePrice());
          map.put("code", product.getCode());
          totalForecast = totalForecast.add(totalPrice);
          mrpForecastSet.add(map);
          continue;
        }
        map.put("product", product);
        map.put("qty", BigDecimal.ZERO);
        map.put("$totalPrice", BigDecimal.ZERO);
        map.put("$unitPrice", product.getSalePrice());
        map.put("code", product.getCode());
        map.put("forecastDate", period.getToDate());
        mrpForecastSet.add(map);
      }
    }
    response.setValue("$mrpForecasts", mrpForecastSet);
    response.setValue("$sopSalesForecast", sopSalesForecast);
    response.setValue("$totalForecast", totalForecast);
    response.setValue(
        "$difference",
        sopSalesForecast
            .subtract(totalForecast)
            .setScale(Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice()));
    response.setValue("$currency", currency);
  }
}
