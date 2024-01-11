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

import com.axelor.apps.base.db.FileSourceConnector;
import com.axelor.apps.base.db.repo.FileSourceConnectorRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.filesourceconnector.FileSourceConnectorService;
import com.axelor.apps.base.translation.ITranslation;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class FileSourceConnectorController {

  public void testConnection(ActionRequest request, ActionResponse response) {

    try {
      FileSourceConnector fileSourceConnector =
          Beans.get(FileSourceConnectorRepository.class)
              .find(request.getContext().asType(FileSourceConnector.class).getId());
      if (fileSourceConnector != null) {

        if (Beans.get(FileSourceConnectorService.class).isValid(fileSourceConnector)) {
          response.setInfo(I18n.get(ITranslation.BASE_FILE_SOURCE_CONNECTOR_SUCCESS_CONNECTION));
          return;
        }
      }
      response.setInfo(I18n.get(ITranslation.BASE_FILE_SOURCE_CONNECTOR_FAILED_CONNECTION));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
