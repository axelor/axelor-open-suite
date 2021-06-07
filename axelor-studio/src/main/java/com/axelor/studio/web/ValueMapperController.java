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
package com.axelor.studio.web;

import com.axelor.apps.tool.ModelTool;
import com.axelor.apps.tool.context.FullContext;
import com.axelor.apps.tool.context.FullContextHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.CallMethod;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Request;
import com.axelor.rpc.Response;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.studio.db.ValueMapper;
import com.axelor.studio.db.repo.ValueMapperRepository;
import javax.script.Bindings;
import javax.script.SimpleBindings;

public class ValueMapperController {

  @CallMethod
  public Response execute(String mapperName) {

    if (mapperName == null) {
      return null;
    }

    ValueMapper mapper = Beans.get(ValueMapperRepository.class).findByName(mapperName);

    if (mapper == null || mapper.getScript() == null) {
      return null;
    }

    Request request = Request.current();

    Model model = (Model) ModelTool.toBean(request.getBeanClass(), request.getContext());
    String modelName = request.getModel();
    if (model instanceof MetaJsonRecord) {
      modelName = ((MetaJsonRecord) model).getJsonModel();
    } else {
      modelName = modelName.substring(modelName.lastIndexOf(".") + 1);
    }

    modelName = modelName.substring(0, 1).toLowerCase() + modelName.substring(1);

    Bindings bindings = new SimpleBindings();
    bindings.put("$ctx", FullContextHelper.class);
    bindings.put(modelName, new FullContext(JPA.find(model.getClass(), model.getId())));

    Object result = new GroovyScriptHelper(bindings).eval(mapper.getScript());

    ActionResponse response = new ActionResponse();
    if (result != null
        && result instanceof FullContext
        && mapper.getScript().startsWith("def rec = $ctx.create(")) {
      FullContext fullContext = (FullContext) result;
      response.setView(
          ActionView.define(I18n.get(fullContext.getTarget().getClass().getSimpleName()))
              .model(fullContext.getTarget().getClass().getName())
              .add("form")
              .add("grid")
              .context("_showRecord", fullContext.get("id"))
              .map());

    } else {
      response.setReload(true);
    }

    return response;
  }
}
