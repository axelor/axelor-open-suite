/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.RecordingImport;
import com.axelor.apps.base.db.repo.RecordingImportRepository;
import com.axelor.apps.base.service.app.DataBackupRestoreService;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.io.File;
import java.io.IOException;

public class RecordingImportController {

  public void importRecordingData(ActionRequest request, ActionResponse response) {

    try {
      RecordingImport recordingImport =
          Beans.get(RecordingImportRepository.class)
              .find(request.getContext().asType(RecordingImport.class).getId());
      File file =
          Beans.get(DataBackupRestoreService.class).restore(recordingImport.getRecordingData());
      if(file!=null) response.setValue("logFile", Beans.get(MetaFiles.class).upload(file));
      } catch (IOException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
