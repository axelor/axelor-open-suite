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
package com.axelor.apps.bpm.script;

import com.axelor.apps.bpm.context.WkfContextHelper;
import java.lang.reflect.Method;
import org.camunda.bpm.engine.impl.javax.el.FunctionMapper;

public class ContextFunctionMapper extends FunctionMapper {

  @Override
  public Method resolveFunction(String prefix, String localName) {

    for (Method method : WkfContextHelper.class.getDeclaredMethods()) {
      if (method.getName().equals(localName)) {
        return method;
      }
    }

    return null;
  }
}
