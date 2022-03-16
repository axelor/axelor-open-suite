/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.wordreport.config;

import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.CompositeScriptHelper;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;
import javax.script.ScriptException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class WordReportGroovyServiceImpl implements WordReportGroovyService {

  @Inject WordReportHelperService helperService;

  @Override // solves $eval:
  public Object evaluate(String expression, Object bean) throws ClassNotFoundException {
    Object result = null;
    try {
      if (expression.contains(".")) {

        Context scriptContext = new Context(Mapper.toMap(bean), bean.getClass());
        result = getScriptResult(scriptContext, expression);

      } else {
        result = validateCondition(expression, bean);
      }
    } catch (NullPointerException e) {
      result = "";
    }

    if (result.getClass().equals(BigDecimal.class)) {
      result = ((BigDecimal) result).setScale(helperService.getBigDecimalScale());
    }
    return result;
  }

  protected Object getScriptResult(Context context, String expression) {
    CompositeScriptHelper csh = new CompositeScriptHelper(context);
    return csh.eval(expression);
  }

  @Override
  public Object validateCondition(String condition, Object bean) {
    Context scriptContext = new Context(Mapper.toMap(bean), bean.getClass());

    if (ObjectUtils.isEmpty(condition)) {
      return "";
    }

    // replace all single quotes to groovy compatible quotes
    if (condition.contains("‘") || condition.contains("’")) {
      condition = condition.replaceAll("‘", "'").replaceAll("’", "'");
    }

    return getScriptResult(scriptContext, condition);
  }

  // for if else condition
  @Override
  public ImmutablePair<String, String> getIfConditionResult(String statement, Object bean)
      throws IOException, AxelorException {

    String condition = "";
    Object flag;

    statement = statement.replaceAll("else if", "\nelse if");
    statement = statement.replace("else", "\nelse");

    List<String> lines = IOUtils.readLines(new StringReader(statement));
    String resultValue = null;
    String operation = " ";
    String value = null;

    for (String line : lines) {
      if (line.startsWith("if") || line.startsWith("else if")) {
        condition = org.apache.commons.lang3.StringUtils.substringBetween(line, "(", ")").trim();
        flag = validateCondition(condition, bean);

        if ((boolean) flag) {
          resultValue = line.substring(line.indexOf("->") + 2).trim();
          break;
        }
      } else if (line.startsWith("else")) {
        resultValue = line.substring(line.indexOf("->") + 2).trim();
      }
    }

    if (StringUtils.isEmpty(resultValue)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.SYNTAX_ERROR) + statement);
    } else if (resultValue.contains(" ") && resultValue.contains("$")) {
      value = resultValue.substring(0, resultValue.indexOf(" "));
      operation = resultValue.substring(resultValue.indexOf(" "));
    } else {
      value = resultValue;
    }

    return new ImmutablePair<>(value, operation.trim());
  }

  @Override // calculates arithmetic operations using javascript engine
  public String calculateFromString(String expression, int bigDecimalScale) throws ScriptException {
    if (StringUtils.isEmpty(expression)) {
      return "";
    }

    return ((BigDecimal) getScriptResult(null, expression)).setScale(bigDecimalScale).toString();
  }
}
