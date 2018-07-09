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
package com.axelor.apps.base.service.imports;

import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.imports.importer.ExcelToCSV;
import com.axelor.apps.base.service.imports.importer.FactoryImporter;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.MetaScanner;
import com.axelor.meta.db.MetaFile;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ImportDemoDataServiceImpl implements ImportDemoDataService {

  @Inject private MetaFiles metaFiles;

  @Inject FactoryImporter factoryImporter;

  @Inject private ExcelToCSV excelToCSV;

  @Override
  public MetaFile importDemoDataExcel(File excelFile)
      throws FileNotFoundException, IOException, AxelorException, ParseException,
          ClassNotFoundException {
    Workbook workBook = new XSSFWorkbook(new FileInputStream(excelFile));
    File tmpFile = File.createTempFile("log", ".txt");
    FileOutputStream out = new FileOutputStream(tmpFile);

    try {

      for (int i = 0; i < workBook.getNumberOfSheets(); i++) {
        Sheet sheet = workBook.getSheetAt(i);

        String[] importDetails = this.getImportDetailsFromSheet(sheet);

        File dataFile = File.createTempFile(importDetails[1], ".csv");

        excelToCSV.writeTOCSV(dataFile, sheet);

        File configFile = File.createTempFile(importDetails[2], ".xml");
        configFile = this.getConfigFile(importDetails[0], configFile, importDetails[2]);

        if (configFile != null && configFile.exists()) {
          MetaFile logMetaFile =
              this.importFileData(dataFile, configFile, importDetails[1], importDetails[2]);
          File file = MetaFiles.getPath(logMetaFile).toFile();
          out.write(("Import: " + importDetails[1]).getBytes());
          ByteStreams.copy(new FileInputStream(file), out);
          out.write("\n\n".getBytes());
        }
      }

    } finally {
      out.close();
    }
    FileInputStream inStream = new FileInputStream(tmpFile);
    return metaFiles.upload(inStream, "log.txt");
  }

  private String[] getImportDetailsFromSheet(Sheet sheet) throws AxelorException {
    String[] importDetails = new String[3];
    Row moduleRow = sheet.getRow(0);
    Row dataFileRow = sheet.getRow(1);
    Row configFileRow = sheet.getRow(2);
    if (moduleRow != null && dataFileRow != null && configFileRow != null) {
      Cell moduleCell = moduleRow.getCell(0);
      Cell dataFileCell = dataFileRow.getCell(0);
      Cell configFileCell = configFileRow.getCell(0);

      this.checkValidity(moduleCell, "module");
      this.checkValidity(configFileCell, "configuation file");
      this.checkValidity(dataFileCell, "data file");

      importDetails[0] = moduleCell.getStringCellValue();
      importDetails[1] = dataFileCell.getStringCellValue();
      importDetails[2] = configFileCell.getStringCellValue();

    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.EXCEL_FILE_FORMAT_ERROR));
    }
    return importDetails;
  }

  private File getConfigFile(String moduleName, File configFile, String configFileName)
      throws IOException, AxelorException {
    String dirNamePattern = "demo".replaceAll("/|\\\\", "(/|\\\\\\\\)");
    List<URL> files = new ArrayList<URL>();

    files.addAll(MetaScanner.findAll(moduleName, dirNamePattern, configFileName + ".xml"));

    if (files.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.FILE_NAME_NOT_EXISTS),
          "configuration file");
    }

    for (URL file : files) {
      FileOutputStream out = new FileOutputStream(configFile);
      try {
        ByteStreams.copy(file.openStream(), out);
      } finally {
        out.close();
      }
    }

    return configFile;
  }

  private void checkValidity(Cell cell, String msg) throws AxelorException {

    if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.FILE_NAME_NOT_EXISTS),
          msg);
    }
  }

  private MetaFile importFileData(
      File dataFile, File configFile, String dataFileName, String configFileName)
      throws IOException, AxelorException {
    ImportConfiguration config = new ImportConfiguration();

    MetaFile configMetaFile =
        metaFiles.upload(new FileInputStream(configFile), configFileName + ".xml");
    MetaFile dataMetaFile = metaFiles.upload(new FileInputStream(dataFile), dataFileName + ".csv");

    config.setBindMetaFile(configMetaFile);
    config.setDataMetaFile(dataMetaFile);
    config.setTypeSelect("csv");

    ImportHistory importHistory = factoryImporter.createImporter(config).run();

    MetaFiles.getPath(configMetaFile).toFile().delete();
    MetaFiles.getPath(dataMetaFile).toFile().delete();

    return importHistory.getLogMetaFile();
  }
}
