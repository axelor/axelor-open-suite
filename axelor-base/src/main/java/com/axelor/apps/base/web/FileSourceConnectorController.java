/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.FileSourceConnector;
import com.axelor.apps.base.db.FileSourceConnectorParameters;
import com.axelor.apps.base.db.repo.FileSourceConnectorParametersRepository;
import com.axelor.apps.base.db.repo.FileSourceConnectorRepository;
import com.axelor.apps.base.service.filesourceconnector.FileSourceConnectorService;
import com.axelor.apps.base.translation.ITranslation;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Arrays;
import java.util.List;

public class FileSourceConnectorController {

  public void testConnection(ActionRequest request, ActionResponse response) {

    try {
      FileSourceConnector fileSourceConnector =
          Beans.get(FileSourceConnectorRepository.class)
              .find(request.getContext().asType(FileSourceConnector.class).getId());
      if (fileSourceConnector != null) {

        if (Beans.get(FileSourceConnectorService.class).isValid(fileSourceConnector)) {
          response.setFlash(I18n.get(ITranslation.BASE_FILE_SOURCE_CONNECTOR_SUCCESS_CONNECTION));
          return;
        }
      }
      response.setFlash(I18n.get(ITranslation.BASE_FILE_SOURCE_CONNECTOR_FAILED_CONNECTION));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  // TODO: To remove (only for tests)
  public void download(ActionRequest request, ActionResponse response) {
    try {

      FileSourceConnectorParameters fileSourceConnectorParameters =
          Beans.get(FileSourceConnectorParametersRepository.class)
              .find(request.getContext().asType(FileSourceConnectorParameters.class).getId());
      if (fileSourceConnectorParameters != null) {
        FileSourceConnector fileSourceConnector =
            fileSourceConnectorParameters.getFileSourceConnector();
        FileSourceConnectorService fileSourceConnectorService =
            Beans.get(FileSourceConnectorService.class);
        fileSourceConnectorService.download(
            fileSourceConnectorService.createSession(fileSourceConnector),
            fileSourceConnectorParameters);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  // TODO: To remove (only for tests)
  public void upload(ActionRequest request, ActionResponse response) {
    try {

      FileSourceConnectorParameters fileSourceConnectorParameters =
          Beans.get(FileSourceConnectorParametersRepository.class)
              .find(request.getContext().asType(FileSourceConnectorParameters.class).getId());
      if (fileSourceConnectorParameters != null) {
        FileSourceConnector fileSourceConnector =
            fileSourceConnectorParameters.getFileSourceConnector();
        FileSourceConnectorService fileSourceConnectorService =
            Beans.get(FileSourceConnectorService.class);

        List<MetaFile> files = Arrays.asList(fileSourceConnectorParameters.getMetaFile());
        fileSourceConnectorService.upload(
            fileSourceConnectorService.createSession(fileSourceConnector),
            fileSourceConnectorParameters,
            files);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
