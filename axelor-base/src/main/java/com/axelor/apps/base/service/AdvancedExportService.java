/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.Context;
import com.axelor.rpc.filter.Filter;
import com.itextpdf.text.DocumentException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public interface AdvancedExportService {

  public String getTargetField(
      Context context, MetaField metaField, String targetField, MetaModel parentMetaModel);

  public Map<Boolean, MetaFile> advancedExportPDF(
      List<Map<String, Object>> advancedExportLines,
      MetaModel metaModel,
      String criteria,
      Integer maxExportLimit,
      Integer queryFetchLimit)
      throws DocumentException, IOException, ClassNotFoundException, AxelorException;

  public Map<Boolean, MetaFile> advancedExportExcel(
      List<Map<String, Object>> advancedExportLines,
      MetaModel metaModel,
      String criteria,
      Integer maxExportLimit,
      Integer queryFetchLimit)
      throws IOException, ClassNotFoundException, DocumentException, AxelorException,
          InvalidFormatException;

  public Map<Boolean, MetaFile> advancedExportCSV(
      List<Map<String, Object>> advancedExportLines,
      MetaModel metaModel,
      String criteria,
      Integer maxExportLimit,
      Integer queryFetchLimit)
      throws IOException, ClassNotFoundException, DocumentException, AxelorException;

  public Filter getJpaSecurityFilter(MetaModel metaModel);

  public String createCriteria(ActionRequest request, int limit);
}
