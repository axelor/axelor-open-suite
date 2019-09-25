/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.ObjectDataConfig;
import com.axelor.apps.base.db.ObjectDataConfigExport;
import com.axelor.apps.base.db.repo.ObjectDataConfigRepository;
import com.axelor.apps.base.service.ObjectDataAnonymizeService;
import com.axelor.apps.base.service.ObjectDataExportService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class ObjectDataExportController {

  @Inject private ObjectDataExportService objectDataExportService;

  @Inject private ObjectDataConfigRepository objectDataConfigRepo;

  @Inject private ObjectDataAnonymizeService objectDataAnonymizeService;

  public void export(ActionRequest request, ActionResponse response) throws AxelorException {

    ObjectDataConfigExport objDataConfigExport =
        request.getContext().asType(ObjectDataConfigExport.class);

    Long objectDataconfigId = objDataConfigExport.getObjectDataConfig().getId();

    ObjectDataConfig objectDataConfig = objectDataConfigRepo.find(objectDataconfigId);
    MetaFile dataFile = objectDataExportService.export(objectDataConfig, objDataConfigExport);

    if (dataFile != null) {
      response.setView(
          ActionView.define(I18n.get("Data"))
              .add(
                  "html",
                  "ws/rest/com.axelor.meta.db.MetaFile/"
                      + dataFile.getId()
                      + "/content/download?v="
                      + dataFile.getVersion())
              .param("download", "true")
              .map());
    }

    response.setCanClose(true);
  }

  public void anonymize(ActionRequest request, ActionResponse response) throws AxelorException {

    Context context = request.getContext();
    Long recordId = Long.parseLong(context.get("modelSelectId").toString());
    Long objectDataconfigId = Long.parseLong(context.get("objectDataConfigId").toString());
    ObjectDataConfig objectDataConfig = objectDataConfigRepo.find(objectDataconfigId);

    objectDataAnonymizeService.anonymize(objectDataConfig, recordId);

    response.setFlash("Data anonymized successfully");

    response.setCanClose(true);
  }
}
