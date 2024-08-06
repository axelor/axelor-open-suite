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
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.Sop;
import com.axelor.apps.production.db.SopLine;
import com.axelor.apps.production.service.SopService;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Singleton
public class SopLineController {

  @SuppressWarnings("unchecked")
  public void fillMrpForecast(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    SopLine sopLine = Mapper.toBean(SopLine.class, (Map<String, Object>) context.get("_sopLine"));
    Sop sop = Mapper.toBean(Sop.class, (Map<String, Object>) context.get("_sop"));

    BigDecimal sopSalesForecast = sopLine.getSopSalesForecast();

    Company company = Beans.get(CompanyRepository.class).find(sop.getCompany().getId());
    Period period = Beans.get(PeriodRepository.class).find(sopLine.getPeriod().getId());
    Currency currency = Beans.get(CurrencyRepository.class).find(sopLine.getCurrency().getId());
    ProductCategory productCategory =
        Beans.get(ProductCategoryRepository.class).find(sop.getProductCategory().getId());

    Set<Map<String, Object>> mrpForecasts =
        Beans.get(SopService.class).fillMrpForecast(productCategory, company, period);
    BigDecimal totalForecast =
        mrpForecasts.stream()
            .map(map -> map.get("$totalPrice"))
            .map(val -> new BigDecimal(val.toString()))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    response.setValue("$mrpForecasts", mrpForecasts);
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
