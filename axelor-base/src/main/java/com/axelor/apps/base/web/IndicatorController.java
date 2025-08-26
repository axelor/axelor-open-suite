/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.IndicatorConfig;
import com.axelor.apps.base.db.repo.IndicatorConfigRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.indicator.IndicatorResultService;
import com.axelor.apps.base.service.indicator.IndicatorService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;

public class IndicatorController {

  public void testQuery(ActionRequest request, ActionResponse response) {
    try {
      IndicatorConfig indicatorConfig = request.getContext().asType(IndicatorConfig.class);
      indicatorConfig = Beans.get(IndicatorConfigRepository.class).find(indicatorConfig.getId());
      Beans.get(IndicatorService.class).executeAndSetQueryResult(indicatorConfig);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void testExpression(ActionRequest request, ActionResponse response) {
    try {
      IndicatorConfig indicatorConfig = request.getContext().asType(IndicatorConfig.class);
      indicatorConfig = Beans.get(IndicatorConfigRepository.class).find(indicatorConfig.getId());
      BigDecimal testIndicator = Beans.get(IndicatorService.class).testExpression(indicatorConfig);
      response.setValue("$testResult", testIndicator);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateResults(ActionRequest request, ActionResponse response) {
    try {
      IndicatorConfig indicatorConfig = request.getContext().asType(IndicatorConfig.class);
      indicatorConfig = Beans.get(IndicatorConfigRepository.class).find(indicatorConfig.getId());
      Beans.get(IndicatorResultService.class).generate(indicatorConfig);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
