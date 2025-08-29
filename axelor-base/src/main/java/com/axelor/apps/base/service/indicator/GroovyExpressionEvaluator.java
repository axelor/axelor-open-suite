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

import com.axelor.apps.base.service.indicator.utils.ResultTransformHelper;
import com.axelor.db.Model;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class GroovyExpressionEvaluator implements ExpressionEvaluator {

  @Override
  public <T extends Model> Optional<BigDecimal> evaluate(
      String expression, T selectedRecord, String targetModelName, Map<String, Object> params) {

    final Map<String, Object> bindings = new HashMap<>();
    if (params != null && !params.isEmpty()) {
      bindings.putAll(params);
    }

    bindings.put(targetModelName, selectedRecord);

    ScriptBindings scriptBindings = new ScriptBindings(bindings);
    GroovyScriptHelper helper = new GroovyScriptHelper(scriptBindings);

    Object result = helper.eval(expression);
    return ResultTransformHelper.toBigDecimal(result);
  }
}
