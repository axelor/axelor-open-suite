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

import com.axelor.apps.base.db.AdvancedExportLine;
import com.axelor.meta.db.MetaModel;
import com.itextpdf.text.DocumentException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface AdvancedExportGenerator {

  public void initialize(
      List<AdvancedExportLine> advancedExportLineList, MetaModel metaModel, File exportFile)
      throws DocumentException, FileNotFoundException, IOException;

  public void generateHeader() throws DocumentException, IOException;

  @SuppressWarnings("rawtypes")
  public void generateBody(List<Map> dataList);

  public void close() throws DocumentException, FileNotFoundException, IOException;
}
