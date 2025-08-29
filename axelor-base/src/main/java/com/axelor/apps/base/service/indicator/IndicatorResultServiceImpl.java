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
import com.axelor.apps.base.db.IndicatorResult;
import com.axelor.apps.base.db.IndicatorResultLine;
import com.axelor.apps.base.db.repo.IndicatorConfigRepository;
import com.axelor.apps.base.db.repo.IndicatorResultLineRepository;
import com.axelor.apps.base.db.repo.IndicatorResultRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.CronExpression;

@Singleton
public class IndicatorResultServiceImpl implements IndicatorResultService {

  protected static final int CRON_LIMIT = 10;
  private static final int BATCH_SIZE = 50;
  private static final String ITERATION_DATE_PARAM = "_iterationDate";

  private final IndicatorResultRepository resultRepo;
  private final IndicatorResultLineRepository lineRepo;
  private final IndicatorQueryService queryService;
  private final ExpressionEvaluatorRegistry evaluatorRegistry;

  @Inject
  public IndicatorResultServiceImpl(
      IndicatorResultRepository resultRepo,
      IndicatorResultLineRepository lineRepo,
      IndicatorQueryService queryService,
      ExpressionEvaluatorRegistry evaluatorRegistry) {
    this.resultRepo = resultRepo;
    this.lineRepo = lineRepo;
    this.queryService = queryService;
    this.evaluatorRegistry = evaluatorRegistry;
  }

  @Override
  public void generateAll(Collection<IndicatorConfig> configs) throws AxelorException {
    if (CollectionUtils.isEmpty(configs)) {
      return;
    }
    for (IndicatorConfig c : configs) {
      generate(c);
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void generate(IndicatorConfig config) throws AxelorException {
    if (StringUtils.isBlank(config.getCron())) {
      generateForDate(config, LocalDate.now());
      return;
    }

    try {
      List<LocalDate> iterationDates = getNextSchedule(config.getCron(), CRON_LIMIT);
      for (LocalDate iterationDate : iterationDates) {
        generateForDate(config, iterationDate);
      }
    } catch (Exception e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Invalid cron expression: %s"),
          config.getCron());
    }
  }

  protected void generateForDate(IndicatorConfig config, LocalDate iterationDate) {

    final IndicatorResult parent = initParent(config, iterationDate);

    final ExpressionEvaluator evaluator = resolveEvaluatorOrLog(config, parent);
    if (evaluator == null) {
      return;
    }

    final List<? extends Model> records = fetchRecordsOrLog(config, parent);
    if (records == null) {
      return;
    }

    final List<Long> recordIds = toIds(records);
    final Class<? extends Model> modelClass = resolveModelClass(config);
    final String modelVar = resolveModelVar(config);

    createLines(config, parent, recordIds, evaluator, modelClass, modelVar, iterationDate);

    JPA.flush();
  }

  @Transactional(rollbackOn = Exception.class)
  protected IndicatorResult initParent(IndicatorConfig config, LocalDate iterationDate) {
    final IndicatorResult parent = new IndicatorResult();
    parent.setIndicatorConfig(config);
    parent.setDate(iterationDate);
    return resultRepo.save(parent);
  }

  protected ExpressionEvaluator resolveEvaluatorOrLog(
      IndicatorConfig config, IndicatorResult parent) {
    final Integer type = config.getExpressionTypeSelect();
    final ExpressionEvaluator evaluator = evaluatorRegistry.get(type);
    if (evaluator == null) {
      parent.setQueryLog(
          String.format(I18n.get("Unsupported expression type: %s"), String.valueOf(type)));
      return null;
    }
    return evaluator;
  }

  protected List<? extends Model> fetchRecordsOrLog(
      IndicatorConfig config, IndicatorResult parent) {
    try {
      return queryService.list(config);
    } catch (Exception e) {
      parent.setQueryLog(String.format(I18n.get("Error running record query: %s"), e.getMessage()));
      return null;
    }
  }

  protected String resolveModelVar(IndicatorConfig config) {
    return config.getTargetModel() != null ? config.getTargetModel().getName() : "record";
  }

  protected void createLines(
      IndicatorConfig config,
      IndicatorResult parent,
      List<Long> ids,
      ExpressionEvaluator evaluator,
      Class<? extends Model> modelClass,
      String modelVar,
      LocalDate iterationDate) {

    int count = 0;

    for (Long id : ids) {
      final Model model = JPA.find(modelClass, id);
      final IndicatorResultLine line = new IndicatorResultLine();
      line.setIndicatorResult(parent);
      line.setMetaModel(config.getTargetModel());
      line.setRelatedId(id);
      evaluateIndicator(config, evaluator, model, modelVar, line, iterationDate);
      evaluateTarget(config, model, modelVar, line, iterationDate);
      lineRepo.save(line);
      count++;
      if (count % BATCH_SIZE == 0) {
        JPA.flush();
        config = JPA.find(IndicatorConfig.class, config.getId());
        parent = JPA.find(IndicatorResult.class, parent.getId());
      }
    }
  }

  protected void evaluateIndicator(
      IndicatorConfig config,
      ExpressionEvaluator evaluator,
      Model model,
      String modelVar,
      IndicatorResultLine line,
      LocalDate iterationDate) {

    String expression = config.getExpression();
    if (StringUtils.isBlank(expression)) {
      line.setIndicator(null);
      return;
    }

    try {
      final java.util.Map<String, Object> params =
          Collections.singletonMap(ITERATION_DATE_PARAM, iterationDate);
      final Optional<BigDecimal> value = evaluator.evaluate(expression, model, modelVar, params);
      line.setIndicator(value.orElse(null));
    } catch (Exception ex) {
      line.setIndicator(null);
      line.setExpressionLog(
          String.format(I18n.get("Error evaluating expression: %s"), ex.getMessage()));
    }
  }

  protected void evaluateTarget(
      IndicatorConfig config,
      Model model,
      String modelVar,
      IndicatorResultLine line,
      LocalDate iterationDate) {

    String targetExpression = config.getTargetExpression();
    if (StringUtils.isBlank(targetExpression)) {
      line.setTarget(null);
      return;
    }

    try {
      ExpressionEvaluator groovyExpressionEvaluator =
          evaluatorRegistry.get(IndicatorConfigRepository.EXPRESSION_TYPE_GROOVY);

      final java.util.Map<String, Object> params =
          Collections.singletonMap(ITERATION_DATE_PARAM, iterationDate);
      final Optional<BigDecimal> value =
          groovyExpressionEvaluator.evaluate(targetExpression, model, modelVar, params);
      line.setTarget(value.orElse(null));
    } catch (Exception ex) {
      line.setTarget(null);
      line.setTargetExpressionLog(
          String.format(I18n.get("Error evaluating target expression: %s"), ex.getMessage()));
    }
  }

  protected Class<? extends Model> resolveModelClass(IndicatorConfig config) {
    if (config.getTargetModel() == null) {
      return null;
    }
    return (Class<? extends Model>) JPA.model(config.getTargetModel().getName());
  }

  protected List<Long> toIds(List<? extends Model> records) {
    return records.stream().map(Model::getId).collect(Collectors.toList());
  }

  protected List<LocalDate> getNextSchedule(String cronExpression, int count)
      throws ParseException {
    CronExpression expression = new CronExpression(cronExpression);
    List<LocalDate> dates = new ArrayList<>();
    Date date = new Date();
    for (int i = 0; i < count; i++) {
      Date next = expression.getNextValidTimeAfter(date);
      if (next == null) {
        break;
      }
      dates.add(next.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
      date = next;
    }
    return dates;
  }
}
