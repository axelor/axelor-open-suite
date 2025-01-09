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
package com.axelor.apps.crm.web;

import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.Tour;
import com.axelor.apps.crm.db.repo.TourRepository;
import com.axelor.apps.crm.service.TourService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.repo.AppBaseRepository;

public class TourController {

  public void setValidated(ActionRequest request, ActionResponse response) {
    Tour tour = request.getContext().asType(Tour.class);
    tour = Beans.get(TourRepository.class).find(tour.getId());
    Beans.get(TourService.class).setValidated(tour);
    response.setReload(true);
  }

  public void showTourOnMap(ActionRequest request, ActionResponse response) {
    try {
      Tour tour = request.getContext().asType(Tour.class);
      AppBaseService appBaseService = Beans.get(AppBaseService.class);
      MapService mapService = Beans.get(MapService.class);
      response.setView(
          ActionView.define(I18n.get(Tour.class.getSimpleName()))
              .add(
                  "html",
                  appBaseService.getAppBase().getMapApiSelect() == AppBaseRepository.MAP_API_GOOGLE
                      ? mapService.getMapURI("tour", tour.getId())
                      : mapService.getOsmMapURI("tour", tour.getId()))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }
}
