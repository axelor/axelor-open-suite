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
package com.axelor.apps.account.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.StringUtils;
import com.axelor.dms.db.DMSFile;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.regex.Pattern;

public class DMSFileController {

  private static final Pattern previewSupportedPattern = Pattern.compile("\\b(?:pdf|image)\\b");

  public void initPdfValues(ActionRequest request, ActionResponse response) {
    try {
      DMSFile dmsFile = request.getContext().asType(DMSFile.class);
      if (dmsFile != null && dmsFile.getId() != null) {

        if ("html".equals(dmsFile.getContentType())) {
          response.setValue("fileType", "text/html");
          response.setValue("contentType", "html");
          response.setValue("typeIcon", "fa fa-file-text-o");
        }
        if ("spreadsheet".equals(dmsFile.getContentType())) {
          response.setValue("fileType", "text/json");
          response.setValue("contentType", "spreadsheet");
          response.setValue("typeIcon", "fa fa-file-excel-o");
        }

        if (dmsFile.getMetaFile() != null) {
          String fileType = dmsFile.getMetaFile().getFileType();
          String fileIcon = Beans.get(MetaFiles.class).fileTypeIcon(dmsFile.getMetaFile());
          response.setValue("fileType", fileType);
          response.setValue("typeIcon", "fa fa-colored " + fileIcon);
          response.setValue("metaFile.sizeText", dmsFile.getMetaFile().getSizeText());

          // Put inlineUrl only if preview for that file type is supported, to prevent
          // auto-downloading
          if (StringUtils.notBlank(fileType) && previewSupportedPattern.matcher(fileType).find()) {
            response.setValue("inlineUrl", String.format("ws/dms/inline/%d", dmsFile.getId()));
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
