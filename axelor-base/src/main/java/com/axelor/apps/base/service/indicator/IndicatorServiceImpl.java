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
import com.axelor.apps.base.db.repo.IndicatorConfigRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.indicator.evaluator.ExpressionEvaluator;
import com.axelor.apps.base.service.indicator.evaluator.ExpressionEvaluatorRegistry;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class IndicatorServiceImpl implements IndicatorService {

  private final IndicatorConfigRepository configRepo;
  private final IndicatorResultService resultService;
  private final IndicatorResultLineService lineService;
  private final IndicatorQueryService queryService;
  private final ExpressionEvaluatorRegistry evaluatorRegistry;
  private final AppBaseService appBaseService;

  @Inject
  public IndicatorServiceImpl(
      IndicatorConfigRepository configRepo,
      IndicatorResultService resultService,
      IndicatorResultLineService lineService,
      IndicatorQueryService queryService,
      ExpressionEvaluatorRegistry evaluatorRegistry,
      AppBaseService appBaseService) {
    this.configRepo = configRepo;
    this.resultService = resultService;
    this.lineService = lineService;
    this.queryService = queryService;
    this.evaluatorRegistry = evaluatorRegistry;
    this.appBaseService = appBaseService;
  }

  @Override
  public void generateAll() throws AxelorException {
    generateAll(configRepo.all().fetch());
  }

  @Override
  public void generateAll(Collection<IndicatorConfig> configs) throws AxelorException {
    if (CollectionUtils.isEmpty(configs)) {
      return;
    }
    for (IndicatorConfig config : configs) {
      generate(config);
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void generate(IndicatorConfig config) throws AxelorException {
    LocalDateTime iterationDateT = appBaseService.getTodayDateTime().toLocalDateTime();

    IndicatorResult parent = resultService.initParent(config, iterationDateT);

    ExpressionEvaluator evaluator = evaluatorRegistry.get(config.getExpressionTypeSelect());
    if (evaluator == null) {
      resultService.updateQueryLog(parent, I18n.get("Unsupported expression type"));
      return;
    }

    List<? extends Model> records = queryService.list(config);
    if (CollectionUtils.isEmpty(records)) {
      resultService.updateQueryLog(parent, I18n.get("No records found for query"));
      return;
    }

    lineService.createLines(config, parent, records, evaluator, iterationDateT);
    configRepo.save(config);
  }
}
