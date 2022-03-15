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
import com.axelor.apps.tool.context.FullContext;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.axelor.meta.db.repo.MetaJsonRecordRepository;
import com.axelor.script.GroovyScriptHelper;
import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;

public class AxelorScriptEngine extends GroovyScriptEngineImpl {

  private volatile AxelorScriptEngineFactory factory;

  AxelorScriptEngine(AxelorScriptEngineFactory factory) {
    super();
    this.factory = factory;
  }

  @Override
  public Object eval(String script, ScriptContext ctx) {
    Bindings bindings = ctx.getBindings(ctx.getScopes().get(0));
    bindings.put("$json", Beans.get(MetaJsonRecordRepository.class));
    bindings.put("$ctx", WkfContextHelper.class);
    bindings.put("$beans", Beans.class);
    bindings.put("__user__", new FullContext(AuthUtils.getUser()));
    return new GroovyScriptHelper(bindings).eval(script);
  }

  @Override
  public CompiledScript compile(String scriptSource) throws ScriptException {
    return null;
  }

  public ScriptEngineFactory getFactory() {
    if (factory == null) {
      synchronized (this) {
        if (factory == null) {
          factory = new AxelorScriptEngineFactory();
        }
      }
    }
    return factory;
  }
}
