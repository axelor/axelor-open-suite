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
package com.axelor.studio.web;

import com.axelor.apps.tool.context.FullContext;
import com.axelor.apps.tool.context.FullContextHelper;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.studio.db.ValueMapper;
import com.axelor.studio.db.repo.ValueMapperRepository;
import com.axelor.studio.service.mapper.ValueMapperService;
import java.util.Map;

public class ValueMapperController {

  public void execute(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    Map<String, Object> valueMapperMap = (Map<String, Object>) context.get("valueMapper");
    ValueMapper mapper =
        Beans.get(ValueMapperRepository.class)
            .find(Long.parseLong(valueMapperMap.get("id").toString()));

    if (mapper == null || mapper.getScript() == null) {
      return;
    }

    String modelName = (String) context.get("modelName");
    Model model = null;
    if (context.get("recordId") != null && modelName != null) {
      Long recordId = Long.parseLong(context.get("recordId").toString());
      model = FullContextHelper.getRepository(modelName).find(recordId);
    }

    Object result = Beans.get(ValueMapperService.class).execute(mapper, model);

    if (result != null
        && result instanceof FullContext
        && mapper.getScript().startsWith("def rec = $ctx.create(")) {
      FullContext fullContext = (FullContext) result;
      Object object = fullContext.getTarget();
      String title = object.getClass().getSimpleName();
      if (object instanceof MetaJsonRecord) {
        title = ((MetaJsonRecord) object).getJsonModel();
      }
      response.setView(
          ActionView.define(I18n.get(title))
              .model(object.getClass().getName())
              .add("form")
              .add("grid")
              .context("_showRecord", fullContext.get("id"))
              .map());
    }
    response.setCanClose(true);
  }
}
