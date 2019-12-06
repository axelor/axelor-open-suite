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

import com.axelor.common.Inflector;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderView;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import java.util.ArrayList;
import java.util.List;

public class ActionBuilderController {

  private Inflector inflector;

  public void setViews(ActionRequest request, ActionResponse response) {

    inflector = Inflector.getInstance();

    ActionBuilder builder = request.getContext().asType(ActionBuilder.class);
    String model = builder.getModel();

    boolean isJson = false;
    if (builder.getIsJson() != null) {
      isJson = builder.getIsJson();
    }
    if (builder.getTypeSelect() == ActionBuilderRepository.TYPE_SELECT_VIEW && model != null) {
      if (!isJson) {
        model = model.substring(model.lastIndexOf('.') + 1);
        model = inflector.dasherize(model);
      }
      List<ActionBuilderView> views = new ArrayList<>();
      addActionBuilderView(views, model, "grid", isJson, 0);
      addActionBuilderView(views, model, "form", isJson, 1);
      response.setValue("actionBuilderViews", views);
    }
  }

  private void addActionBuilderView(
      List<ActionBuilderView> views, String model, String type, boolean isJson, int sequence) {

    String viewName = model + "-" + type;
    if (isJson) {
      viewName = "custom-model-" + model + "-" + type;
    }

    MetaView view = Beans.get(MetaViewRepository.class).findByName(viewName);
    if (view == null) {
      return;
    }

    ActionBuilderView builderView = new ActionBuilderView();
    builderView.setViewName(view.getName());
    builderView.setViewType(view.getType());
    builderView.setSequence(sequence);

    views.add(builderView);
  }
}
