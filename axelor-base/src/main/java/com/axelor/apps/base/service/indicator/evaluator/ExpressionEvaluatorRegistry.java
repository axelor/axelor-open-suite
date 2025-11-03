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
package com.axelor.apps.base.service.indicator.evaluator;

import com.axelor.apps.base.db.repo.IndicatorConfigRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Singleton
public class ExpressionEvaluatorRegistry {

  private final Map<Integer, ExpressionEvaluator> evaluators = new HashMap<>();

  @Inject
  public ExpressionEvaluatorRegistry(Set<ExpressionEvaluator> evaluatorSet) {
    for (ExpressionEvaluator evaluator : evaluatorSet) {
      if (evaluator instanceof GroovyExpressionEvaluator) {
        evaluators.put(IndicatorConfigRepository.EXPRESSION_TYPE_GROOVY, evaluator);
      } else if (evaluator instanceof JpqlExpressionEvaluator) {
        evaluators.put(IndicatorConfigRepository.EXPRESSION_TYPE_JPQL, evaluator);
      } else if (evaluator instanceof JsExpressionEvaluator) {
        evaluators.put(IndicatorConfigRepository.EXPRESSION_TYPE_JS, evaluator);
      }
    }
  }

  public ExpressionEvaluator get(Integer type) {
    return evaluators.get(type);
  }
}
