/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.auth.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.db.IMessage;
import com.axelor.auth.db.Permission;
import com.axelor.auth.service.PermissionService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
import java.util.stream.Collectors;

public class PermissionController {

  public void checkPermissionsObject(ActionRequest request, ActionResponse response) {
    try {
      List<Long> invalidObjectIds = Beans.get(PermissionService.class).checkPermissionsObject();

      if (ObjectUtils.notEmpty(invalidObjectIds)) {
        response.setView(
            ActionView.define(I18n.get("Permissions"))
                .model(Permission.class.getName())
                .add("grid", "permission-grid")
                .add("form", "permission-form")
                .domain(
                    "self.id in ("
                        + invalidObjectIds.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(","))
                        + ")")
                .map());
      } else {
        response.setInfo(I18n.get(IMessage.ALL_PERMISSIONS_OBJECT_OK));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
