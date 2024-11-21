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
package com.axelor.meta.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.db.IMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.repo.MetaGroupMenuAssistantRepository;
import com.axelor.meta.service.MetaGroupMenuAssistantService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.time.LocalDateTime;

@Singleton
public class MetaGroupMenuAssistantController {

  public void createGroupMenuFile(ActionRequest request, ActionResponse response) {
    try {
      Long groupMenuAssistantId = (Long) request.getContext().get("id");
      Beans.get(MetaGroupMenuAssistantService.class)
          .createGroupMenuFile(
              Beans.get(MetaGroupMenuAssistantRepository.class).find(groupMenuAssistantId));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void importGroupMenu(ActionRequest request, ActionResponse response) {
    try {
      Long groupMenuAssistantId = (Long) request.getContext().get("id");
      String errorLog =
          Beans.get(MetaGroupMenuAssistantService.class)
              .importGroupMenu(
                  Beans.get(MetaGroupMenuAssistantRepository.class).find(groupMenuAssistantId));

      response.setValue("log", errorLog);
      if (errorLog.isEmpty()) {
        response.setInfo(I18n.get(IMessage.IMPORT_OK));
        response.setValue("importDate", LocalDateTime.now());
      } else {
        response.setInfo(I18n.get(IMessage.ERR_IMPORT));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
