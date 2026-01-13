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
import com.axelor.apps.base.service.indicator.IndicatorChartService;
import com.axelor.apps.base.service.indicator.IndicatorConfigService;
import com.axelor.common.Inflector;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class IndicatorController {

  public void testQuery(ActionRequest request, ActionResponse response) {
    try {
      IndicatorConfig indicatorConfig = request.getContext().asType(IndicatorConfig.class);
      indicatorConfig = Beans.get(IndicatorConfigRepository.class).find(indicatorConfig.getId());
      Beans.get(IndicatorConfigService.class).executeAndSetQueryResult(indicatorConfig);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void testExpression(ActionRequest request, ActionResponse response) {
    try {
      IndicatorConfig indicatorConfig = request.getContext().asType(IndicatorConfig.class);
      indicatorConfig = Beans.get(IndicatorConfigRepository.class).find(indicatorConfig.getId());
      BigDecimal testIndicator =
          Beans.get(IndicatorConfigService.class).testExpression(indicatorConfig);
      response.setValue("$testResult", testIndicator);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fetchResultLinesForChart(ActionRequest request, ActionResponse response) {
    try {
      String domainAction = (String) request.getContext().get("_domainAction");
      Optional<Long> configIdOpt =
          Optional.ofNullable(domainAction)
              .map(s -> StringUtils.substringAfterLast(s, "-"))
              .filter(StringUtils::isNumeric)
              .map(Long::parseLong)
              .filter(id -> id > 0);
      Long relatedId =
          Optional.ofNullable(request.getContext().get("id"))
              .map(Object::toString)
              .map(Long::valueOf)
              .orElse(0l);

      if (configIdOpt.isEmpty() || relatedId == 0l) {
        response.setData(Collections.emptyList());
        return;
      }

      Long configId = configIdOpt.get();

      IndicatorConfig config = Beans.get(IndicatorConfigRepository.class).find(configId);
      List<Map<String, Object>> data =
          Beans.get(IndicatorChartService.class).fetchData(config, relatedId);
      response.setData(data);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
      response.setData(Collections.emptyList());
    }
  }

  public void viewIndicatorResults(ActionRequest request, ActionResponse response) {
    String modelName = request.getContext().getContextClass().getSimpleName();
    String dasherizedModelName = Inflector.getInstance().dasherize(modelName);
    response.setView(
        ActionView.define(I18n.get("Indicators"))
            .add("form", String.format("indicator-result-viewer-%s-form", dasherizedModelName))
            .model(request.getModel())
            .context("_showRecord", request.getContext().get("id"))
            .map());
  }
}
