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
package com.axelor.apps.base.service.mapConfigurator;

import com.axelor.apps.base.db.MapView;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.utils.helpers.MetaActionHelper;
import com.google.inject.persist.Transactional;
import jakarta.ws.rs.core.UriBuilder;

public class MapViewServiceImpl implements MapViewService {

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void computeMapActionView(MapView mapView) {
    MetaMenu metaMenu = mapView.getMetaMenu();

    if (metaMenu == null || metaMenu.getAction() != null) {
      return;
    }

    String actionName = String.format("menu.map.view.%d", mapView.getId());

    ActionView actionView =
        ActionView.define(mapView.getName())
            .name(actionName)
            .add(
                "html",
                UriBuilder.fromUri("base/map-viewer")
                    .queryParam("mapId", String.valueOf(mapView.getId()))
                    .build()
                    .toString())
            .get();

    metaMenu.setAction(
        MetaActionHelper.actionToMetaAction(actionView, actionName, "action-view", "axelor-base"));
  }
}
