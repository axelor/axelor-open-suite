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
package com.axelor.apps.base.service.indicator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.IndicatorConfig;
import com.axelor.apps.base.db.repo.IndicatorConfigRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.indicator.evaluator.ExpressionEvaluator;
import com.axelor.apps.base.service.indicator.evaluator.ExpressionEvaluatorRegistry;
import com.axelor.common.StringUtils;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Singleton
public class IndicatorConfigServiceImpl implements IndicatorConfigService {

  private final IndicatorConfigRepository indicatorConfigRepository;
  private final IndicatorQueryService queryService;
  private final IndicatorExportService exportService;
  private final ExpressionEvaluatorRegistry evaluatorRegistry;

  @Inject
  public IndicatorConfigServiceImpl(
      IndicatorConfigRepository indicatorConfigRepository,
      IndicatorQueryService queryService,
      IndicatorExportService exportService,
      ExpressionEvaluatorRegistry evaluatorRegistry) {
    this.indicatorConfigRepository = indicatorConfigRepository;
    this.queryService = queryService;
    this.exportService = exportService;
    this.evaluatorRegistry = evaluatorRegistry;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public <T extends Model> void executeAndSetQueryResult(IndicatorConfig indicatorConfig)
      throws AxelorException {
    List<T> result = queryService.list(indicatorConfig);
    indicatorConfig.setQueryResult(exportService.toCsv(result));
    indicatorConfigRepository.save(indicatorConfig);
  }

  @Override
  public <T extends Model> BigDecimal testExpression(IndicatorConfig indicatorConfig)
      throws AxelorException {
    try {
      String expression = indicatorConfig.getExpression();
      if (StringUtils.isBlank(expression)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get("Indicator expression cannot be null or empty"));
      }

      T selectedRecord = queryService.loadSelected(indicatorConfig);
      if (selectedRecord == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get("No test record found for indicator configuration"));
      }

      Integer type = indicatorConfig.getExpressionTypeSelect();
      ExpressionEvaluator evaluator = evaluatorRegistry.get(type);
      if (evaluator == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(I18n.get("Unsupported expression type: %s"), type));
      }
      String modelVar = indicatorConfig.getTargetModel().getName();
      return evaluator.evaluate(expression, selectedRecord, modelVar).orElse(BigDecimal.ZERO);
    } catch (AxelorException e) {
      throw e;
    } catch (Exception e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(I18n.get("Error evaluating indicator expression: %s"), e.getMessage()));
    }
  }
}
