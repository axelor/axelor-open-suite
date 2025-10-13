/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.quality.db.RequiredDocument;
import com.axelor.apps.quality.db.repo.RequiredDocumentRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.apps.quality.service.RequiredDocumentDMSService;
import com.axelor.apps.quality.service.RequiredDocumentExportService;
import com.axelor.apps.quality.service.RequiredDocumentFileOnChangeService;
import com.axelor.apps.quality.service.RequiredDocumentVersionService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionExport;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RequiredDocumentController {

  public void onChangeMetaFile(ActionRequest request, ActionResponse response) {
    try {
      RequiredDocumentRepository repository = Beans.get(RequiredDocumentRepository.class);
      RequiredDocumentFileOnChangeService fileOnChangeService =
          Beans.get(RequiredDocumentFileOnChangeService.class);

      RequiredDocument requiredDocument = request.getContext().asType(RequiredDocument.class);
      requiredDocument = repository.find(requiredDocument.getId());
      fileOnChangeService.onChange(requiredDocument);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setInlineUrl(ActionRequest request, ActionResponse response) {
    RequiredDocument requiredDocument = request.getContext().asType(RequiredDocument.class);
    response.setValue(
        "$inlineUrl", Beans.get(RequiredDocumentDMSService.class).getInlineUrl(requiredDocument));
  }

  public void createNewVersion(ActionRequest request, ActionResponse response) {
    RequiredDocumentRepository requiredDocumentRepository =
        Beans.get(RequiredDocumentRepository.class);
    RequiredDocumentVersionService requiredDocumentService =
        Beans.get(RequiredDocumentVersionService.class);

    RequiredDocument requiredDocument = request.getContext().asType(RequiredDocument.class);
    requiredDocument = requiredDocumentRepository.find(requiredDocument.getId());
    RequiredDocument newVersion = requiredDocumentService.createNewVersion(requiredDocument);

    response.setCanClose(true);
    response.setView(
        ActionView.define(I18n.get("Required document"))
            .model(RequiredDocument.class.getName())
            .add("form", "required-document-form")
            .add("grid", "required-document-grid")
            .context("_showRecord", newVersion.getId())
            .map());
  }

  public void activateVersion(ActionRequest request, ActionResponse response) {
    RequiredDocumentRepository requiredDocumentRepository =
        Beans.get(RequiredDocumentRepository.class);
    RequiredDocumentVersionService requiredDocumentService =
        Beans.get(RequiredDocumentVersionService.class);

    RequiredDocument requiredDocument = request.getContext().asType(RequiredDocument.class);
    requiredDocument = requiredDocumentRepository.find(requiredDocument.getId());
    requiredDocumentService.activateVersion(requiredDocument);

    response.setReload(true);
  }

  @SuppressWarnings("unchecked")
  public void exportFiles(ActionRequest request, ActionResponse response) {
    try {
      List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
      if (ObjectUtils.isEmpty(idList)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(QualityExceptionMessage.NO_RECORD_SELECTED_TO_EXPORT));
      }
      Path exportFile = Beans.get(RequiredDocumentExportService.class).exportFile(idList);
      Path exportDirPath = Paths.get(ActionExport.getExportPath().getAbsolutePath());
      Path relativePath = exportDirPath.relativize(exportFile);
      response.setExportFile(relativePath.toString());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
