/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.AdvancedExportLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class CsvExportGenerator extends AdvancedExportGenerator {

  private CSVWriter csvWriter;

  private String[] totalCols;

  private AdvancedExport advancedExport;

  private File exportFile;

  private String exportFileName;

  public CsvExportGenerator(AdvancedExport advancedExport) throws AxelorException {
    this.advancedExport = advancedExport;
    exportFileName = advancedExport.getMetaModel().getName() + ".csv";
    try {
      exportFile = File.createTempFile(advancedExport.getMetaModel().getName(), ".csv");
      csvWriter = new CSVWriter(new FileWriter(exportFile, true), ';');
    } catch (IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
    totalCols = new String[advancedExport.getAdvancedExportLineList().size()];
  }

  @Override
  public void generateHeader() {
    int index = 0;
    for (AdvancedExportLine advancedExportLine : advancedExport.getAdvancedExportLineList()) {
      totalCols[index++] = I18n.get(advancedExportLine.getTitle());
    }
    csvWriter.writeNext(totalCols);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void generateBody(List<List> dataList) {
    for (List listObj : dataList) {
      for (int colIndex = 0; colIndex < listObj.size(); colIndex++) {
        Object value = listObj.get(colIndex);
        String columnValue = null;
        if (!(value == null || value.equals(""))) {
          if (value instanceof BigDecimal) columnValue = convertDecimalValue(value);
          else columnValue = value.toString();
        }
        totalCols[colIndex] = columnValue;
      }
      csvWriter.writeNext(totalCols);
    }
  }

  @Override
  public void close() throws AxelorException {
    try {
      csvWriter.close();
    } catch (IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  @Override
  public AdvancedExport getAdvancedExport() {
    return advancedExport;
  }

  @Override
  public File getExportFile() {
    return exportFile;
  }

  @Override
  public String getFileName() {
    return exportFileName;
  }
}
