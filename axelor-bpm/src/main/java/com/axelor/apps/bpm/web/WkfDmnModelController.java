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
package com.axelor.apps.bpm.web;

import com.axelor.apps.bpm.context.WkfContextHelper;
import com.axelor.apps.bpm.db.WkfDmnModel;
import com.axelor.apps.bpm.db.repo.WkfDmnModelRepository;
import com.axelor.apps.dmn.service.DmnDeploymentService;
import com.axelor.apps.dmn.service.DmnExportService;
import com.axelor.apps.dmn.service.DmnImportService;
import com.axelor.apps.dmn.service.DmnService;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

public class WkfDmnModelController {

  public void deploy(ActionRequest request, ActionResponse response) {

    WkfDmnModel dmnModel = request.getContext().asType(WkfDmnModel.class);

    dmnModel = Beans.get(WkfDmnModelRepository.class).find(dmnModel.getId());
    Beans.get(DmnDeploymentService.class).deploy(dmnModel);

    response.setReload(true);
  }

  public void executeDmn(ActionRequest request, ActionResponse response) throws AxelorException {

    Context context = request.getContext();

    String decisionId = (String) ((Map<String, Object>) context.get("dmnTable")).get("decisionId");

    String ctxModel = (String) context.get("ctxModel");
    Long ctxRecordId = Long.parseLong(context.get("ctxRecordId").toString());

    if (ctxRecordId == null || ctxModel == null) {
      return;
    }

    Model model = WkfContextHelper.getRepository(ctxModel).find(ctxRecordId);

    Beans.get(DmnService.class).executeDmn(decisionId, model);

    response.setCanClose(true);
  }

  public void createOutputToFieldScript(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    String decisionId = (String) context.get("decisionId");

    String ctxModel = (String) context.get("ctxModel");

    String searchWith = (String) context.get("searchWith");

    String ifMultiple = (String) context.get("ifMultiple");

    String resultVariable = (String) context.get("resultVariable");

    String script =
        Beans.get(DmnService.class)
            .createOutputToFieldScript(
                decisionId, ctxModel, searchWith, ifMultiple, resultVariable);

    response.setValue("script", script);
  }

  public void exportDmnTable(ActionRequest request, ActionResponse response) {
    try {
      WkfDmnModel dmnModel = request.getContext().asType(WkfDmnModel.class);

      dmnModel = Beans.get(WkfDmnModelRepository.class).find(dmnModel.getId());
      File file = Beans.get(DmnExportService.class).exportDmnTable(dmnModel);

      FileInputStream inStream = new FileInputStream(file);
      MetaFile exportFile =
          Beans.get(MetaFiles.class).upload(inStream, dmnModel.getName() + ".xlsx");
      inStream.close();
      file.delete();

      if (exportFile != null) {
        response.setView(
            ActionView.define(I18n.get("Export file"))
                .model(WkfDmnModel.class.getName())
                .add(
                    "html",
                    "ws/rest/com.axelor.meta.db.MetaFile/"
                        + exportFile.getId()
                        + "/content/download?v="
                        + exportFile.getVersion())
                .param("download", "true")
                .map());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("rawtypes")
  public void importDmnTable(ActionRequest request, ActionResponse response) {
    try {
      MetaFile dataFile =
          Beans.get(MetaFileRepository.class)
              .find(
                  Long.parseLong(
                      ((Map) request.getContext().get("dataFile")).get("id").toString()));

      Long dmnModelId = Long.parseLong(request.getContext().get("_dmnModelId").toString());
      WkfDmnModel dmnModel = Beans.get(WkfDmnModelRepository.class).find(dmnModelId);

      Beans.get(DmnImportService.class).importDmnTable(dataFile, dmnModel);

      response.setCanClose(true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
