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
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaModel;
import com.itextpdf.text.DocumentException;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CsvExportGenerator implements AdvancedExportGenerator {

  private List<AdvancedExportLine> advancedExportLineList = null;

  private CSVWriter csvWriter;

  private String[] totalCols;

  private int index;

  @Override
  public void initialize(
      List<AdvancedExportLine> advancedExportLineList, MetaModel metaModel, File exportFile)
      throws DocumentException, FileNotFoundException, IOException {
    this.advancedExportLineList = advancedExportLineList;
    csvWriter = new CSVWriter(new FileWriter(exportFile, true), ';');
    totalCols = new String[advancedExportLineList.size()];
    index = 0;
  }

  @Override
  public void generateHeader() throws DocumentException, IOException {
    for (AdvancedExportLine advancedExportLine : advancedExportLineList) {
      totalCols[index++] = I18n.get(advancedExportLine.getTitle());
    }
    csvWriter.writeNext(totalCols);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void generateBody(List<Map> dataList) {
    index = 0;
    for (Map<String, Object> field : dataList) {
      String[] allCols = field.keySet().toArray(new String[field.size()]);
      Integer[] allColIndices = new Integer[allCols.length];

      for (int j = 0; j < allCols.length; j++) {
        String col = allCols[j];
        allColIndices[j] = Integer.parseInt(col.replace("Col_", ""));
      }
      Arrays.sort(allColIndices);

      for (Integer colIndex : allColIndices) {
        String colName = "Col_" + colIndex;
        Object value = field.get(colName);
        String columnValue = null;
        if (!(value == null || value.equals(""))) {
          if (value instanceof BigDecimal)
            columnValue = AdvancedExportServiceImpl.convertDecimalValue(value);
          else columnValue = value.toString();
        }
        totalCols[index++] = columnValue;
      }
      csvWriter.writeNext(totalCols);
      index = 0;
    }
  }

  @Override
  public void close() throws DocumentException, FileNotFoundException, IOException {
    csvWriter.close();
  }
}
