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
package com.axelor.studio.service.mapper;

import com.axelor.apps.tool.StringTool;
import com.axelor.apps.tool.context.FullContext;
import com.axelor.apps.tool.context.FullContextHelper;
import com.axelor.db.Model;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.studio.db.ValueMapper;
import javax.script.Bindings;
import javax.script.SimpleBindings;

public class ValueMapperService {

  public Object execute(ValueMapper mapper, Model model) {

    if (model == null || mapper == null) {
      return null;
    }

    String modelName = getModelVariable(model);

    Bindings bindings = new SimpleBindings();
    bindings.put("$ctx", FullContextHelper.class);
    bindings.put(modelName, new FullContext(model));
    bindings.put(modelName + "Id", model.getId());

    Object result = new GroovyScriptHelper(bindings).eval(mapper.getScript());

    return result;
  }

  private String getModelVariable(Model model) {

    String modelName = null;
    if (model instanceof MetaJsonRecord) {
      modelName = ((MetaJsonRecord) model).getJsonModel();
    } else {
      modelName = model.getClass().getSimpleName();
    }

    modelName = StringTool.toFirstLower(modelName);

    return modelName;
  }
}
