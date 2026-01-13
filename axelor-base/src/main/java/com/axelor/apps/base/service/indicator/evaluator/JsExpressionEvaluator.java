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

import com.axelor.apps.base.service.indicator.utils.ResultTransformHelper;
import com.axelor.common.Inflector;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Model;
import com.axelor.script.JavaScriptScriptHelper;
import com.axelor.script.ScriptBindings;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class JsExpressionEvaluator implements ExpressionEvaluator {

  @Override
  public <T extends Model> Optional<BigDecimal> evaluate(
      String expression, T selectedRecord, String targetModelName, Map<String, Object> params) {

    final Map<String, Object> bindings = new HashMap<>();
    if (params != null && !params.isEmpty()) {
      bindings.putAll(params);
    }

    String recordParamName = Inflector.getInstance().camelize(targetModelName, true);
    bindings.put(recordParamName, selectedRecord);

    ScriptBindings scriptBindings = new ScriptBindings(bindings);
    JavaScriptScriptHelper helper = new JavaScriptScriptHelper(scriptBindings);

    Object result = helper.eval(expression);
    return ResultTransformHelper.toBigDecimal(result);
  }

  public boolean test(String expression, Map<String, Object> params) {
    final Map<String, Object> bindings = new HashMap<>();
    if (ObjectUtils.notEmpty(params)) {
      bindings.putAll(params);
    }

    ScriptBindings scriptBindings = new ScriptBindings(bindings);
    JavaScriptScriptHelper helper = new JavaScriptScriptHelper(scriptBindings);
    Object result = helper.eval(expression);

    if (result instanceof Boolean) {
      return (Boolean) result;
    }
    if (result instanceof Number) {
      return ((Number) result).doubleValue() != 0d;
    }
    if (result instanceof String) {
      return Boolean.parseBoolean((String) result);
    }
    return result != null;
  }
}
