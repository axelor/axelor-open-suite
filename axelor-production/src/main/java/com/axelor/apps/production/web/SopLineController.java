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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.service.SopService;
import com.axelor.apps.production.service.SopServiceImpl;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.Map;

@Singleton
public class SopLineController {

  @SuppressWarnings("unchecked")
  public void fillMrpForecast(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();

    Map<String, Object> sopLineMap = (Map<String, Object>) context.get("_sopLine");
    BigDecimal sopSalesForecast = new BigDecimal(sopLineMap.get("sopSalesForecast").toString());
    response.setValue(
        "$mrpForecasts",
        Beans.get(SopService.class)
            .fillMrpForecast(
                Beans.get(ProductCategoryRepository.class)
                    .find(
                        Long.parseLong(
                            (((Map<String, Object>) context.get("_productCategory"))
                                .get("id")
                                .toString()))),
                sopLineMap,
                Beans.get(CompanyRepository.class)
                    .find(
                        Long.parseLong(
                            (((Map<String, Object>) context.get("_company"))
                                .get("id")
                                .toString())))));

    response.setValue("$sopSalesForecast", sopSalesForecast);
    response.setValue("$totalForecast", SopServiceImpl.TOTAL_FORECAST);
    response.setValue(
        "$difference",
        sopSalesForecast
            .subtract(SopServiceImpl.TOTAL_FORECAST)
            .setScale(Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice()));
    response.setValue(
        "$currency",
        Beans.get(CurrencyRepository.class)
            .find(
                Long.parseLong(
                    ((Map<String, Object>) sopLineMap.get("currency")).get("id").toString())));
  }
}
