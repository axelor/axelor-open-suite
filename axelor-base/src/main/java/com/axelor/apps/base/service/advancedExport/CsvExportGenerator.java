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
package com.axelor.apps.base.service.advancedExport;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.AdvancedExportLine;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.csv.CSVFile;
import com.axelor.i18n.I18n;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.csv.CSVPrinter;

public class CsvExportGenerator extends AdvancedExportGenerator {

  private CSVFile csvFormat;

  private CSVPrinter printer;

  private String[] totalCols;

  private AdvancedExport advancedExport;

  private File exportFile;

  private String exportFileName;

  public CsvExportGenerator(AdvancedExport advancedExport) throws AxelorException {
    this.advancedExport = advancedExport;
    exportFileName = advancedExport.getMetaModel().getName() + ".csv";
    try {
      exportFile = File.createTempFile(advancedExport.getMetaModel().getName(), ".csv");
      csvFormat = CSVFile.DEFAULT.withDelimiter(';').withQuoteAll().withFirstRecordAsHeader();
      printer = csvFormat.write(exportFile);
    } catch (IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
    totalCols = new String[advancedExport.getAdvancedExportLineList().size()];
  }

  @Override
  public void generateHeader() throws AxelorException {
    try {
      int index = 0;
      for (AdvancedExportLine advancedExportLine : advancedExport.getAdvancedExportLineList()) {
        totalCols[index++] = I18n.get(advancedExportLine.getTitle());
      }
      printer.printRecord(Arrays.asList(totalCols));
    } catch (IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void generateBody(List<List> dataList) throws AxelorException {
    try {
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
        printer.printRecord(Arrays.asList(totalCols));
      }
    } catch (IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  @Override
  public void close() throws AxelorException {
    try {
      printer.close();
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
