/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.message.service;

import com.axelor.apps.message.db.TemplateContext;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;

public class TemplateContextService {

  public Object computeTemplateContext(String groovyScript, Context values) {

    ScriptHelper scriptHelper = new GroovyScriptHelper(values);

    return scriptHelper.eval(groovyScript);
  }

  public Object computeTemplateContext(TemplateContext templateContext, Context values) {
    return this.computeTemplateContext(templateContext.getValue(), values);
  }
}
