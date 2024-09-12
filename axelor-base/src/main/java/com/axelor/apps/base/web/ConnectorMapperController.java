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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.ConnectorMapper;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.meta.MetaViewService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ConnectorMapperController {

  @SuppressWarnings("unchecked")
  public void showAOSRecord(ActionRequest request, ActionResponse response) {
    try {
      ConnectorMapper connectorMapper = request.getContext().asType(ConnectorMapper.class);
      Class<? extends Model> modelClass =
          (Class<? extends Model>) Class.forName(connectorMapper.getMetaModel().getFullName());
      Model record = JPA.find(modelClass, connectorMapper.getModelId());
      if (record == null) {
        return;
      }
      response.setView(Beans.get(MetaViewService.class).getActionView(modelClass, record.getId()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
