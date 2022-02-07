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
package com.axelor.studio.service;

import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderLine;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.google.common.base.Strings;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class ExportService {

  private ExportService() {
    throw new IllegalStateException("Should not be instantiated.");
  }

  public static String getImage(MetaFile metaFile) {

    if (metaFile != null) {
      File file = MetaFiles.getPath(metaFile).toFile();
      if (file != null) {
        try {
          byte[] img = IOUtils.toByteArray(new FileInputStream(file));
          return Base64.getEncoder().encodeToString(img);
        } catch (IOException e) {
          TraceBackService.trace(e);
        }
      }
    }

    return "";
  }

  @SuppressWarnings("deprecation")
  public static String exportActionBuilderLines(List<ActionBuilderLine> lines, int count) {

    String xml = "";

    String indent = "\n" + Strings.repeat("\t", count);
    String indentPlus = "\n" + Strings.repeat("\t", count + 1);
    for (ActionBuilderLine line : lines) {

      String source = "";
      String target = "";

      if (line.getParent() == null) {
        ActionBuilder builder = line.getActionBuilder();
        if (builder != null) {
          target = builder.getTargetModel();
          source = builder.getModel();
          if (builder.getTypeSelect() == ActionBuilderRepository.TYPE_SELECT_UPDATE) {
            target = builder.getModel();
          }
        }
      } else {
        ActionBuilderLine parent = line.getParent();
        if (parent.getMetaField() != null) target = parent.getMetaField().getTypeName();
        if (parent.getMetaJsonField() != null && parent.getMetaJsonField().getTargetModel() != null)
          target = parent.getMetaJsonField().getTargetModel();
        if (parent.getMetaJsonField() != null
            && parent.getMetaJsonField().getTargetJsonModel() != null)
          target = parent.getMetaJsonField().getTargetJsonModel().getName();
        if (parent.getValueField() != null)
          source = parent.getValueField().getMetaModel().getFullName();
        if (parent.getValueJson() != null && parent.getValueJson().getTargetModel() != null)
          source = parent.getValueJson().getTargetModel();
        if (parent.getValueJson() != null && parent.getValueJson().getTargetJsonModel() != null)
          source = parent.getValueJson().getTargetJsonModel().getName();
      }

      xml +=
          indent
              + "<line>"
              + indentPlus
              + "<target>"
              + target
              + "</target>"
              + indentPlus
              + "<source>"
              + source
              + "</source>"
              + indentPlus
              + "<metaJsonField>"
              + (line.getMetaJsonField() != null ? line.getMetaJsonField().getName() : "")
              + "</metaJsonField>"
              + indentPlus
              + "<metaField>"
              + (line.getMetaField() != null ? line.getMetaField().getName() : "")
              + "</metaField>"
              + indentPlus
              + "<valueJson>"
              + (line.getValueJson() != null ? line.getValueJson().getName() : "")
              + "</valueJson>"
              + indentPlus
              + "<valueField>"
              + (line.getValueField() != null ? line.getValueField().getName() : "")
              + "</valueField>"
              + indentPlus
              + "<value>"
              + (line.getValue() != null ? line.getValue() : "")
              + "</value>"
              + indentPlus
              + "<conditionText>"
              + (line.getConditionText() != null
                  ? StringEscapeUtils.escapeXml(
                      StringEscapeUtils.escapeXml(line.getConditionText()))
                  : "")
              + "</conditionText>"
              + indentPlus
              + "<filter>"
              + (line.getFilter() != null ? line.getFilter() : "")
              + "</filter>"
              + indentPlus
              + "<validationTypeSelect>"
              + (line.getValidationTypeSelect() != null ? line.getValidationTypeSelect() : "")
              + "</validationTypeSelect>"
              + indentPlus
              + "<validationMsg>"
              + (line.getValidationMsg() != null ? line.getValidationMsg() : "")
              + "</validationMsg>"
              + indentPlus
              + "<name>"
              + (line.getName() != null ? line.getName() : "")
              + "</name>"
              + indentPlus
              + "<dummy>"
              + (line.getDummy() != null ? line.getDummy() : "")
              + "</dummy>"
              + indentPlus
              + "<subLines>"
              + exportActionBuilderLines(line.getSubLines(), count + 2)
              + indentPlus
              + "</subLines>"
              + indent
              + "</line>";
    }

    return StringEscapeUtils.unescapeXml(xml);
  }
}
