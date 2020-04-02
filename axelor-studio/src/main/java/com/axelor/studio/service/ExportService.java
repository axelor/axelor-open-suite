/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderLine;
import com.google.common.base.Strings;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;

public class ExportService {

  public static String getImage(MetaFile metaFile) {

    if (metaFile != null) {
      File file = MetaFiles.getPath(metaFile).toFile();
      if (file != null) {
        try {
          byte[] img = IOUtils.toByteArray(new FileInputStream(file));
          return Base64.getEncoder().encodeToString(img);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return "";
  }

  public static String exportActionBuilderLines(List<ActionBuilderLine> lines, int count) {

    String xml = "";

    String indent = "\n" + Strings.repeat("\t", count);
    for (ActionBuilderLine line : lines) {

      String source = "";
      String target = "";

      if (line.getParent() == null) {
        ActionBuilder builder = line.getActionBuilder();
        target = builder.getTargetModel();
        source = builder.getModel();
        if (builder.getTypeSelect() == 1) {
          target = builder.getModel();
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
              + indent
              + "<target>"
              + target
              + "</target>"
              + indent
              + "<source>"
              + source
              + "</source>"
              + indent
              + "<metaJsonField>"
              + (line.getMetaJsonField() != null ? line.getMetaJsonField().getName() : "")
              + "</metaJsonField>"
              + indent
              + "<metaField>"
              + (line.getMetaField() != null ? line.getMetaField().getName() : "")
              + "</metaField>"
              + indent
              + "<valueJson>"
              + (line.getValueJson() != null ? line.getValueJson().getName() : "")
              + "</valueJson>"
              + indent
              + "<valueField>"
              + (line.getValueField() != null ? line.getValueField().getName() : "")
              + "</valueField>"
              + indent
              + "<value>"
              + (line.getValue() != null ? line.getValue() : "")
              + "</value>"
              + indent
              + "<conditionText>"
              + (line.getConditionText() != null ? line.getConditionText() : "")
              + "</conditionText>"
              + indent
              + "<filter>"
              + (line.getFilter() != null ? line.getFilter() : "")
              + "</filter>"
              + indent
              + "<validationTypeSelect>"
              + (line.getValidationTypeSelect() != null ? line.getValidationTypeSelect() : "")
              + "</validationTypeSelect>"
              + indent
              + "<validationMsg>"
              + (line.getValidationMsg() != null ? line.getValidationMsg() : "")
              + "</validationMsg>"
              + indent
              + "<name>"
              + (line.getName() != null ? line.getName() : "")
              + "</name>"
              + indent
              + "<dummy>"
              + (line.getDummy() != null ? line.getDummy() : "")
              + "</dummy>"
              + indent
              + "<subLines>"
              + exportActionBuilderLines(line.getSubLines(), count + 1)
              + "</subLines>"
              + "</line>";
    }

    return StringEscapeUtils.unescapeXml(xml);
  }
}
