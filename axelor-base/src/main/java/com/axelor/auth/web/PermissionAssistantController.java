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
import com.axelor.auth.db.PermissionAssistant;
import com.axelor.auth.db.repo.PermissionAssistantRepository;
import com.axelor.auth.service.PermissionAssistantService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class PermissionAssistantController {

  public void createFile(ActionRequest request, ActionResponse response) {
    try {
      Long permissionAssistantId = (Long) request.getContext().get("id");
      Beans.get(PermissionAssistantService.class)
          .createFile(Beans.get(PermissionAssistantRepository.class).find(permissionAssistantId));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void importPermissions(ActionRequest request, ActionResponse response) {
    try {
      Long permissionAssistantId = (Long) request.getContext().get("id");
      String errors =
          Beans.get(PermissionAssistantService.class)
              .importPermissions(
                  Beans.get(PermissionAssistantRepository.class).find(permissionAssistantId));
      response.setValue("importDate", LocalDateTime.now());
      response.setValue("log", errors);

      if (errors.isEmpty()) {
        response.setInfo(I18n.get(IMessage.IMPORT_OK));
      } else {
        response.setInfo(I18n.get(IMessage.ERR_IMPORT));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillObjects(ActionRequest request, ActionResponse response) {
    try {
      PermissionAssistant assistant = request.getContext().asType(PermissionAssistant.class);
      MetaField metaField = assistant.getMetaField();

      if (metaField != null
          && (assistant.getObjectSet() == null || assistant.getObjectSet().isEmpty())) {

        List<MetaModel> models =
            Beans.get(MetaModelRepository.class)
                .all()
                .filter(
                    "self.metaFields.relationship = 'ManyToOne'"
                        + " and self.metaFields.typeName = ?1",
                    metaField.getTypeName())
                .fetch();

        Set<MetaModel> objectSet = new HashSet<>();
        objectSet.addAll(models);
        response.setValue("objectSet", objectSet);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
