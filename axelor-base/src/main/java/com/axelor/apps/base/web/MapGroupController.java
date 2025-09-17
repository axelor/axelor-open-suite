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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.MapGroup;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MapGroupController {

  public void previewRecords(ActionRequest request, ActionResponse response) {
    MapGroup mapGroup = request.getContext().asType(MapGroup.class);
    MetaModel metaModel = mapGroup.getMetaModel();
    response.setView(
        ActionView.define(I18n.get(metaModel.getName()))
            .model(metaModel.getFullName())
            .add("grid")
            .add("form")
            .domain(mapGroup.getFilter())
            .param("popup", Boolean.TRUE.toString())
            .map());
  }
}
