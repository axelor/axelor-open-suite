/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.excelreport.utility;

import com.axelor.exception.AxelorException;
import java.io.IOException;
import javax.script.ScriptException;
import org.apache.commons.lang3.tuple.ImmutablePair;

public interface ExcelReportGroovyService {

  public boolean getConditionResult(String statement, Object bean);

  public ImmutablePair<String, String> getIfConditionResult(String statement, Object bean)
      throws IOException, AxelorException;

  public String calculateFromString(String expression, int bigDecimalScale) throws ScriptException;

  public Object validateCondition(String condition, Object bean);
}
