/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.studio.service.builder.ViewBuilderService;
import com.google.inject.Inject;

public class ViewBuilderController {

  @Inject private ViewBuilderService viewBuilderService;

  @Inject private ViewBuilderRepository viewBuilderRepo;

  public void preview(ActionRequest request, ActionResponse response) {

    try {

      ViewBuilder viewBuilder = request.getContext().asType(ViewBuilder.class);

      viewBuilderService.genereateMetaView(viewBuilderRepo.find(viewBuilder.getId()), null);

      String model = viewBuilderService.getModelName(viewBuilder, null);

      if (model != null) {
        response.setView(
            ActionView.define(viewBuilder.getTitle())
                .model(model)
                .add(viewBuilder.getViewType(), viewBuilder.getName())
                .map());
      }
      response.setReload(true);
    } catch (AxelorException e) {
      response.setFlash(e.getMessage());
    }
  }
}
