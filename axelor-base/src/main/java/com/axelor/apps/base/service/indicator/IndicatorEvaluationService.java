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
import com.axelor.apps.base.db.IndicatorResultLine;
import com.axelor.apps.base.service.indicator.evaluator.ExpressionEvaluator;
import com.axelor.db.Model;
import java.util.Map;

public interface IndicatorEvaluationService {
  void evaluateIndicator(
      IndicatorConfig config,
      ExpressionEvaluator evaluator,
      Model model,
      IndicatorResultLine line,
      Map<String, Object> params);

  void evaluateTarget(
      IndicatorConfig config, Model model, IndicatorResultLine line, Map<String, Object> params);

  void evaluateAlert(
      IndicatorConfig config, Model model, IndicatorResultLine line, Map<String, Object> params);
}
