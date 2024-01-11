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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ObjectDataConfig;
import com.axelor.apps.base.db.ObjectDataConfigExport;
import com.axelor.apps.base.db.repo.ObjectDataConfigRepository;
import com.axelor.apps.base.service.ObjectDataAnonymizeService;
import com.axelor.apps.base.service.ObjectDataExportService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class ObjectDataExportController {

  public void export(ActionRequest request, ActionResponse response) throws AxelorException {

    ObjectDataConfigExport objectDataConfigExport =
        request.getContext().asType(ObjectDataConfigExport.class);

    Long objectDataConfigId = objectDataConfigExport.getObjectDataConfig().getId();

    ObjectDataConfig objectDataConfig =
        Beans.get(ObjectDataConfigRepository.class).find(objectDataConfigId);
    MetaFile dataFile =
        Beans.get(ObjectDataExportService.class).export(objectDataConfig, objectDataConfigExport);

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
    ObjectDataConfig objectDataConfig =
        Beans.get(ObjectDataConfigRepository.class).find(objectDataconfigId);

    Beans.get(ObjectDataAnonymizeService.class).anonymize(objectDataConfig, recordId);

    response.setInfo("Data anonymized successfully");

    response.setCanClose(true);
  }
}
