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

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.stream.Stream;

public class BatchController {

  public void showBatches(ActionRequest request, ActionResponse response) {
    try {
      String field =
          Stream.of(Mapper.of(Batch.class).getProperties())
              .filter(p -> p.getJavaType() == request.getContext().getContextClass())
              .findFirst()
              .map(Property::getName)
              .orElse(null);

      ActionViewBuilder actionViewBuilder =
          ActionView.define(I18n.get("Batchs"))
              .model(Batch.class.getName())
              .add("grid", "batch-grid")
              .add("form", "batch-form");

      if (field != null && request.getContext().get("id") != null) {
        actionViewBuilder.domain(String.format("self.%s.id = :_batchId", field));
        actionViewBuilder.context(
            "_batchId", Long.parseLong(request.getContext().get("id").toString()));
      } else {
        actionViewBuilder.domain("self.id = 0");
      }
      response.setView(actionViewBuilder.map());
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }
}
