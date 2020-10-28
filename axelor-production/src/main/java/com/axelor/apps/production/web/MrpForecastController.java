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
package com.axelor.apps.production.web;

import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.service.MrpForecastProductionService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MrpForecastController {

  @Inject MrpForecastProductionService mrpForecastProductionService;
  @Inject ProductRepository productRepo;

  @SuppressWarnings("unchecked")
  public void generateMrpForecast(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    LinkedHashMap<String, Object> sopLineMap =
        (LinkedHashMap<String, Object>) context.get("_sopLine");
    LinkedHashMap<String, Object> periodMap =
        (LinkedHashMap<String, Object>) sopLineMap.get("period");
    Period period =
        Beans.get(PeriodRepository.class).find(Long.parseLong(periodMap.get("id").toString()));

    ArrayList<LinkedHashMap<String, Object>> mrpForecastList =
        (ArrayList<LinkedHashMap<String, Object>>) context.get("mrpForecasts");

    LinkedHashMap<String, Object> stockLocationMap =
        (LinkedHashMap<String, Object>) context.get("stockLocation");

    StockLocation stockLocation =
        Beans.get(StockLocationRepository.class)
            .find(Long.parseLong(stockLocationMap.get("id").toString()));
    if (mrpForecastList != null && !mrpForecastList.isEmpty()) {
      mrpForecastProductionService.generateMrpForecast(
          period,
          mrpForecastList,
          stockLocation,
          MrpForecastRepository.TECHNICAL_ORIGIN_CREATED_FROM_SOP);
    }
    response.setCanClose(true);
  }

  public void computeTotalForecast(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    @SuppressWarnings("unchecked")
    ArrayList<LinkedHashMap<String, Object>> mrpForecastList =
        (ArrayList<LinkedHashMap<String, Object>>) context.get("mrpForecasts");
    BigDecimal totalForecast = BigDecimal.ZERO;
    BigDecimal sopSalesForecast = new BigDecimal(context.get("sopSalesForecast").toString());
    if (mrpForecastList != null) {
      for (LinkedHashMap<String, Object> mrpForecastItem : mrpForecastList) {
        BigDecimal qty = new BigDecimal(mrpForecastItem.get("qty").toString());
        if (qty.compareTo(BigDecimal.ZERO) == 0) {
          continue;
        }
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> productMap =
            (LinkedHashMap<String, Object>) mrpForecastItem.get("product");
        BigDecimal unitPrice = new BigDecimal(productMap.get("salePrice").toString());
        totalForecast = totalForecast.add(qty.multiply(unitPrice));
      }
    }
    response.setValue("$totalForecast", totalForecast);
    response.setValue(
        "$difference",
        sopSalesForecast
            .subtract(totalForecast)
            .setScale(Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice())
            .abs());
  }

  public void resetMrpForecasts(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    @SuppressWarnings("unchecked")
    ArrayList<LinkedHashMap<String, Object>> mrpForecastList =
        (ArrayList<LinkedHashMap<String, Object>>) context.get("mrpForecasts");
    BigDecimal totalForecast = BigDecimal.ZERO;
    BigDecimal sopSalesForecast = new BigDecimal(context.get("sopSalesForecast").toString());
    if (mrpForecastList != null) {
      for (LinkedHashMap<String, Object> mrpForecastItem : mrpForecastList) {
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> productMap =
            (LinkedHashMap<String, Object>) mrpForecastItem.get("product");
        BigDecimal unitPrice = new BigDecimal(productMap.get("salePrice").toString());
        mrpForecastItem.put("qty", BigDecimal.ZERO);
        mrpForecastItem.put("$totalPrice", BigDecimal.ZERO);
        mrpForecastItem.put("$unitPrice", unitPrice);
      }
    }
    response.setValue("$mrpForecasts", mrpForecastList);
    response.setValue("$totalForecast", totalForecast);
    response.setValue("$difference", sopSalesForecast);
  }
}
