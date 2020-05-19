/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.repo.WkfRepository;

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
}
