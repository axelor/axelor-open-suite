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
package com.axelor.apps.base.service.advancedExport;

import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.rpc.Context;
import com.axelor.rpc.filter.Filter;
import com.itextpdf.text.DocumentException;
import java.io.IOException;
import java.util.Map;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public interface AdvancedExportService {

  public static final String LANGUAGE_FR = "fr";

  public static final String PDF_EXTENSION = ".pdf";
  public static final String EXCEL_EXTENSION = ".xlsx";
  public static final String CSV_EXTENSION = ".csv";

  public static final String PDF = "PDF";
  public static final String EXCEL = "EXCEL";
  public static final String CSV = "CSV";

  public String getTargetField(Context context, MetaField metaField, MetaModel parentMetaModel);

  public Map<Boolean, MetaFile> getAdvancedExport(
      AdvancedExport advancedExport, String criteria, String fileType)
      throws ClassNotFoundException, DocumentException, IOException, AxelorException,
          InvalidFormatException;

  public Filter getJpaSecurityFilter(MetaModel metaModel);
}
