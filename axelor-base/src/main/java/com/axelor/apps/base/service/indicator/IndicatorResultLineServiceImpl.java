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

import com.axelor.apps.base.db.IndicatorConfig;
import com.axelor.apps.base.db.IndicatorResult;
import com.axelor.apps.base.db.IndicatorResultLine;
import com.axelor.apps.base.db.repo.IndicatorResultLineRepository;
import com.axelor.apps.base.service.indicator.evaluator.ExpressionEvaluator;
import com.axelor.db.Model;
import com.axelor.meta.db.MetaModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class IndicatorResultLineServiceImpl implements IndicatorResultLineService {

  private final IndicatorResultLineRepository lineRepo;
  private final IndicatorEvaluationService evaluationService;

  @Inject
  public IndicatorResultLineServiceImpl(
      IndicatorResultLineRepository lineRepo, IndicatorEvaluationService evaluationService) {
    this.lineRepo = lineRepo;
    this.evaluationService = evaluationService;
  }

  @Override
  public void createLines(
      IndicatorConfig config,
      IndicatorResult parent,
      List<? extends Model> records,
      ExpressionEvaluator evaluator,
      LocalDateTime iterationDate) {

    if (records.isEmpty()) {
      return;
    }

    MetaModel targetModel = config.getTargetModel();
    Map<String, Object> params = new HashMap<>();
    params.put("_iterationDate", iterationDate);

    for (Model record : records) {
      IndicatorResultLine line = new IndicatorResultLine();
      line.setIndicatorResult(parent);
      line.setMetaModel(targetModel);
      line.setRelatedId(record.getId());

      evaluationService.evaluateIndicator(config, evaluator, record, line, params);
      evaluationService.evaluateTarget(config, record, line, params);
      evaluationService.evaluateAlert(config, record, line, params);

      lineRepo.save(line);
    }
  }
}
