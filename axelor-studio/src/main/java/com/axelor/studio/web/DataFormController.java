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
package com.axelor.studio.web;

import com.axelor.apps.base.service.app.AppService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.DataForm;
import com.axelor.studio.service.DataFormMetaModelService;
import com.axelor.studio.service.DataFormService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataFormController {

  public void setModelFormViewDomain(ActionRequest request, ActionResponse response) {
    try {
      DataForm dataForm = request.getContext().asType(DataForm.class);
      String domainFilter = Beans.get(DataFormMetaModelService.class).getDomainFilter(dataForm);
      response.setAttr("modelFormView", "domain", domainFilter);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateFields(ActionRequest request, ActionResponse response) {
    try {
      DataForm dataForm = request.getContext().asType(DataForm.class);
      Beans.get(DataFormService.class).generateFields(dataForm);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateHtmlForm(ActionRequest request, ActionResponse response) {
    try {
      DataForm dataForm = request.getContext().asType(DataForm.class);
      String htmlFormString = Beans.get(DataFormService.class).generateHtmlForm(dataForm);

      if (ObjectUtils.isEmpty(htmlFormString)) {
        return;
      }

      String fileName =
          dataForm.getCustom()
              ? dataForm.getMetaJsonModel().getName()
              : dataForm.getMetaModel().getName();

      Path file = MetaFiles.createTempFile(fileName, ".html");
      Files.write(file, htmlFormString.getBytes());
      response.setValue("formText", htmlFormString);
      String relativePath =
          Paths.get(Beans.get(AppService.class).getDataExportDir()).relativize(file).toString();
      response.setExportFile(relativePath);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
