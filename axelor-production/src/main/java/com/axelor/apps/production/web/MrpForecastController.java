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
package com.axelor.apps.production.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.Sop;
import com.axelor.apps.production.db.SopLine;
import com.axelor.apps.production.service.MrpForecastProductionService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.supplychain.db.MrpForecast;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MrpForecastController {

  @SuppressWarnings("unchecked")
  public void generateMrpForecast(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    SopLine sopLine = Mapper.toBean(SopLine.class, (Map<String, Object>) context.get("_sopLine"));
    Period period = Beans.get(PeriodRepository.class).find(sopLine.getPeriod().getId());

    List<MrpForecast> mrpForecastList =
        ((List<Map<String, Object>>) context.get("mrpForecasts"))
            .stream()
                .map(map -> Mapper.toBean(MrpForecast.class, map))
                .collect(Collectors.toList());
    StockLocation stockLocation =
        Beans.get(StockLocationRepository.class)
            .find(
                Long.parseLong(
                    ((LinkedHashMap<String, Object>) context.get("stockLocation"))
                        .get("id")
                        .toString()));

    if (CollectionUtils.isNotEmpty(mrpForecastList)) {
      Beans.get(MrpForecastProductionService.class)
          .generateMrpForecast(
              period,
              mrpForecastList,
              stockLocation,
              MrpForecastRepository.TECHNICAL_ORIGIN_CREATED_FROM_SOP);
    }
    response.setCanClose(true);
  }

  @SuppressWarnings("unchecked")
  public void computeTotalForecast(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    Sop sop = Mapper.toBean(Sop.class, (Map<String, Object>) context.get("_sop"));
    Company company = Beans.get(CompanyRepository.class).find(sop.getCompany().getId());

    List<MrpForecast> mrpForecastList =
        ((List<Map<String, Object>>) context.get("mrpForecasts"))
            .stream()
                .map(map -> Mapper.toBean(MrpForecast.class, map))
                .collect(Collectors.toList());
    BigDecimal totalForecast =
        Beans.get(MrpForecastProductionService.class)
            .computeTotalForecast(mrpForecastList, company);
    response.setValue("$totalForecast", totalForecast);
    response.setValue(
        "$difference",
        (new BigDecimal(context.get("sopSalesForecast").toString()))
            .subtract(totalForecast)
            .setScale(Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice())
            .abs());
  }

  @SuppressWarnings("unchecked")
  public void resetMrpForecasts(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    Sop sop = Mapper.toBean(Sop.class, (Map<String, Object>) context.get("_sop"));
    Company company = Beans.get(CompanyRepository.class).find(sop.getCompany().getId());
    List<MrpForecast> mrpForecastList =
        ((List<Map<String, Object>>) context.get("mrpForecasts"))
            .stream()
                .map(map -> Mapper.toBean(MrpForecast.class, map))
                .collect(Collectors.toList());

    response.setValue(
        "$mrpForecasts",
        Beans.get(MrpForecastProductionService.class).resetMrpForecasts(mrpForecastList, company));
    response.setValue("$totalForecast", BigDecimal.ZERO);
    response.setValue("$difference", new BigDecimal(context.get("sopSalesForecast").toString()));
  }
}
