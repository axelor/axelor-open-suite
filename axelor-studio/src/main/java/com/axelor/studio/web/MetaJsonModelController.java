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
package com.axelor.studio.web;

import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.repo.WkfRepository;
import com.axelor.studio.service.StudioMetaService;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import java.util.stream.Collectors;

public class MetaJsonModelController {

  public void openWorkflow(ActionRequest request, ActionResponse response) {

    MetaJsonModel jsonModel = request.getContext().asType(MetaJsonModel.class);

    Wkf wkf =
        Beans.get(WkfRepository.class)
            .all()
            .filter("self.model = ?1", jsonModel.getName())
            .fetchOne();

    ActionViewBuilder builder =
        ActionView.define("Workflow").add("form", "wkf-form").model("com.axelor.studio.db.Wkf");

    if (wkf == null) {
      builder.context("_jsonModel", jsonModel.getName());
    } else {
      builder.context("_showRecord", wkf.getId());
    }

    response.setView(builder.map());
  }

  public void trackJsonField(ActionRequest request, ActionResponse response) {
    try {
      MetaJsonModel jsonModel = request.getContext().asType(MetaJsonModel.class);

      String jsonFieldTracking =
          request.getContext().get("jsonFieldTracking") != null
              ? request.getContext().get("jsonFieldTracking").toString()
              : "";

      if (!jsonFieldTracking.isEmpty()) {
        Beans.get(StudioMetaService.class)
            .trackingFields(jsonModel, jsonFieldTracking, "Field added");
        response.setValue("$jsonFieldTracking", null);
        return;
      }

      Beans.get(StudioMetaService.class).trackJsonField(jsonModel);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setJsonFieldTracking(ActionRequest request, ActionResponse response) {

    try {
      MetaJsonModel jsonModel = request.getContext().asType(MetaJsonModel.class);

      if (jsonModel.getId() != null || CollectionUtils.isEmpty(jsonModel.getFields())) {
        response.setValue("$jsonFieldTracking", null);
        return;
      }

      String jsonFields =
          jsonModel
              .getFields()
              .stream()
              .map(list -> list.getName())
              .collect(Collectors.joining(", "));

      response.setValue("$jsonFieldTracking", jsonFields);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
