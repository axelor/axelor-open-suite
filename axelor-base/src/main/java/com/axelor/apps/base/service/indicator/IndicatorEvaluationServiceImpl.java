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

import com.axelor.apps.base.db.IndicatorAlert;
import com.axelor.apps.base.db.IndicatorConfig;
import com.axelor.apps.base.db.IndicatorResultLine;
import com.axelor.apps.base.db.repo.IndicatorAlertRepository;
import com.axelor.apps.base.db.repo.IndicatorResultLineRepository;
import com.axelor.apps.base.service.indicator.evaluator.ExpressionEvaluator;
import com.axelor.apps.base.service.indicator.evaluator.ExpressionEvaluatorRegistry;
import com.axelor.apps.base.service.indicator.evaluator.GroovyExpressionEvaluator;
import com.axelor.apps.base.service.indicator.evaluator.JsExpressionEvaluator;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.google.inject.Singleton;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class IndicatorEvaluationServiceImpl implements IndicatorEvaluationService {

  private static final Logger LOG = LoggerFactory.getLogger(IndicatorEvaluationServiceImpl.class);

  private static final String BINDING_INDICATOR = "indicator";
  private static final String BINDING_TARGET = "target";

  private final ExpressionEvaluatorRegistry evaluatorRegistry;
  private final IndicatorResultLineRepository lineRepo;
  private final IndicatorAlertRepository alertRepo;

  @Inject
  public IndicatorEvaluationServiceImpl(
      ExpressionEvaluatorRegistry evaluatorRegistry,
      IndicatorResultLineRepository lineRepo,
      IndicatorAlertRepository alertRepo) {
    this.evaluatorRegistry = evaluatorRegistry;
    this.lineRepo = lineRepo;
    this.alertRepo = alertRepo;
  }

  @Override
  public void evaluateIndicator(
      IndicatorConfig config,
      ExpressionEvaluator evaluator,
      Model model,
      IndicatorResultLine line,
      Map<String, Object> params) {

    String expression = config.getExpression();
    if (StringUtils.isBlank(expression)) {
      line.setIndicator(null);
      line.setExpressionLog(I18n.get("Indicator expression is empty"));
      return;
    }
    try {
      String modelVar = config.getTargetModel().getName();
      Optional<BigDecimal> value = evaluator.evaluate(expression, model, modelVar, params);
      line.setIndicator(value.orElse(BigDecimal.ZERO).setScale(10, RoundingMode.HALF_UP));
    } catch (Exception ex) {
      line.setIndicator(null);
      line.setExpressionLog(
          String.format(I18n.get("Error evaluating expression: %s"), ex.getMessage()));
    }
  }

  @Override
  public void evaluateTarget(
      IndicatorConfig config, Model model, IndicatorResultLine line, Map<String, Object> params) {

    String targetExpression = config.getTargetExpression();
    if (StringUtils.isBlank(targetExpression)) {
      line.setTarget(null);
      return;
    }

    try {
      ExpressionEvaluator evaluator = evaluatorRegistry.get(config.getTargetExpressionTypeSelect());

      String modelVar = config.getTargetModel().getName();
      Optional<BigDecimal> value = evaluator.evaluate(targetExpression, model, modelVar, params);

      line.setTarget(value.orElse(BigDecimal.ZERO).setScale(10, RoundingMode.HALF_UP));
    } catch (Exception ex) {
      line.setTarget(null);
      line.setTargetExpressionLog(
          String.format(I18n.get("Error evaluating target expression: %s"), ex.getMessage()));
    }
  }

  @Override
  public void evaluateAlert(
      IndicatorConfig config, Model model, IndicatorResultLine line, Map<String, Object> params) {

    String alertExpression = config.getAlertExpression();
    if (StringUtils.isBlank(alertExpression)) {
      return;
    }

    try {
      ExpressionEvaluator evaluator = evaluatorRegistry.get(config.getAlertExpressionTypeSelect());

      String modelVar = config.getTargetModel().getName();
      Map<String, Object> bindings = buildAlertBindings(params, model, line, modelVar);

      boolean matches = evaluateAlertExpression(evaluator, alertExpression, bindings);

      if (!matches) {
        return;
      }

      IndicatorAlert alert = new IndicatorAlert();
      alert.setStatusSelect(IndicatorAlertRepository.STATUS_NEW);
      alert.setResultLine(line);
      alertRepo.save(alert);

      LOG.debug("IndicatorConfig[{}] - alert created for line id={}", config.getId(), line.getId());

    } catch (Exception ex) {
      line.setAlertExpressionLog(
          String.format(I18n.get("Error evaluating alert expression: %s"), ex.getMessage()));
      lineRepo.save(line);
      LOG.debug(
          "IndicatorConfig[{}] - alert evaluation failed: {}", config.getId(), ex.getMessage());
    }
  }

  protected Map<String, Object> buildAlertBindings(
      Map<String, Object> params, Model model, IndicatorResultLine line, String modelVar) {
    Map<String, Object> bindings = new HashMap<>();
    if (ObjectUtils.notEmpty(params)) {
      bindings.putAll(params);
    }
    bindings.put(BINDING_INDICATOR, line.getIndicator());
    bindings.put(BINDING_TARGET, line.getTarget());
    bindings.put(modelVar, model);
    return bindings;
  }

  protected boolean evaluateAlertExpression(
      ExpressionEvaluator evaluator, String expression, Map<String, Object> bindings) {
    if (evaluator instanceof GroovyExpressionEvaluator) {
      return ((GroovyExpressionEvaluator) evaluator).test(expression, bindings);
    }
    if (evaluator instanceof JsExpressionEvaluator) {
      return ((JsExpressionEvaluator) evaluator).test(expression, bindings);
    }
    throw new IllegalArgumentException(
        I18n.get("Selected alert expression type does not support boolean evaluation."));
  }
}
