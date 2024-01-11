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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.DataConfigLine;
import com.axelor.apps.base.db.ObjectDataConfig;
import com.axelor.apps.base.db.ObjectDataConfigExport;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.Inflector;
import com.axelor.common.csv.CSVFile;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.schema.views.Selection.Option;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectDataExportServiceImpl implements ObjectDataExportService {

  private final Logger logger = LoggerFactory.getLogger(ObjectDataExportServiceImpl.class);

  private Inflector inflector = Inflector.getInstance();

  @Inject private MetaFiles metaFiles;

  @Override
  public MetaFile export(
      ObjectDataConfig objectDataConfig, ObjectDataConfigExport objDataConfigExport)
      throws AxelorException {

    Long recordId = objDataConfigExport.getModelSelectId();
    String language = objDataConfigExport.getLangSelect();
    String format = objDataConfigExport.getExportFormatSelect();

    try {
      logger.debug(
          "Exporting data for model: {}, id: {}, language: {}, format: {}",
          objectDataConfig.getModelSelect(),
          recordId,
          language,
          format);

      Map<String, List<String[]>> data = createData(objectDataConfig, recordId, language);

      if (format != null && format.equals("csv")) {
        return writeCSV(data);
      } else {
        return writeExcel(data);
      }
    } catch (ClassNotFoundException | IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  private Map<String, List<String[]>> createData(
      ObjectDataConfig objectDataConfig, Long recordId, String language)
      throws ClassNotFoundException, AxelorException {

    Map<String, List<String[]>> data = new HashMap<>();

    for (DataConfigLine line : objectDataConfig.getDataConfigLineList()) {

      MetaModel metaModel = line.getMetaModel();

      logger.debug("Create data for: {}", metaModel.getName());

      Class<? extends Model> modelClass = ObjectDataCommonService.findModelClass(metaModel);

      Query<? extends Model> query =
          ObjectDataCommonService.createQuery(recordId, line, modelClass);

      ResourceBundle bundle = ObjectDataCommonService.getResourceBundle(language);

      String[][] fieldsData = createFieldsData(line.getToExportMetaFieldSet(), bundle);

      Map<String, String> selectMap = getSelectMap(Mapper.of(modelClass));

      List<String[]> dataList = fetchData(fieldsData, query, selectMap, bundle);

      data.put(line.getTabName(), dataList);
    }

    return data;
  }

  private Map<String, String> getSelectMap(Mapper mapper) {

    Map<String, String> selectionMap = new HashMap<>();

    for (Property property : mapper.getProperties()) {
      String selection = property.getSelection();
      if (!Strings.isNullOrEmpty(selection)) {
        selectionMap.put(property.getName(), selection);
      }
    }

    return selectionMap;
  }

  private String[][] createFieldsData(Set<MetaField> metaFields, ResourceBundle bundle)
      throws ClassNotFoundException {

    List<String> names = new ArrayList<>();
    List<String> labels = new ArrayList<>();

    for (MetaField field : metaFields) {
      String label = field.getLabel();
      String name = field.getName();

      if (Strings.isNullOrEmpty(label)) {
        label = inflector.humanize(name);
      }

      if (field.getRelationship() != null) {
        name = name + "." + ObjectDataCommonService.getNameColumn(field);
      }

      names.add(name);

      String translated = bundle.getString(label);

      if (!Strings.isNullOrEmpty(translated)) {
        labels.add(translated);
      } else {
        labels.add(label);
      }
    }

    logger.debug("Fields: {}", names);
    logger.debug("Labels: {}", labels);

    return new String[][] {
      names.toArray(new String[names.size()]), labels.toArray(new String[labels.size()])
    };
  }

  private List<String[]> fetchData(
      String[][] fieldsData,
      Query<? extends Model> query,
      Map<String, String> selectMap,
      ResourceBundle bundle) {

    List<String[]> dataList = new ArrayList<>();
    dataList.add(fieldsData[1]);

    List<Map> records = query.select(fieldsData[0]).fetch(0, 0);

    for (Map<String, Object> recordMap : records) {

      List<String> datas = new ArrayList<>();

      for (String field : fieldsData[0]) {
        Object object = recordMap.get(field);
        if (object == null) {
          datas.add("");
          continue;
        }
        if (selectMap.containsKey(field)) {
          String selection = selectMap.get(field);
          Option option = MetaStore.getSelectionItem(selection, object.toString());
          if (option == null) {
            datas.add("");
            continue;
          }
          String optionTitle = option.getTitle();
          optionTitle = bundle.getString(optionTitle);
          if (optionTitle != null) {
            datas.add(optionTitle);
          } else {
            datas.add(option.getTitle());
          }
        } else {
          datas.add(object.toString());
        }
      }

      dataList.add(datas.toArray(new String[fieldsData[0].length]));
    }

    return dataList;
  }

  protected MetaFile writeCSV(Map<String, List<String[]>> data) throws IOException {

    File zipFile = MetaFiles.createTempFile("Data", ".zip").toFile();
    try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile))) {

      for (Entry<String, List<String[]>> modelEntry : data.entrySet()) {
        String key = modelEntry.getKey();
        File modelFile = MetaFiles.createTempFile(key, ".csv").toFile();
        CSVFile csvFormat =
            CSVFile.DEFAULT.withDelimiter(';').withQuoteAll().withFirstRecordAsHeader();
        try (CSVPrinter printer = csvFormat.write(modelFile)) {
          printer.printRecords(modelEntry.getValue());
        }
        zout.putNextEntry(new ZipEntry(key + ".csv"));
        zout.write(IOUtils.toByteArray(new FileInputStream(modelFile)));
        zout.closeEntry();
      }
    }

    return metaFiles.upload(zipFile);
  }

  protected MetaFile writeExcel(Map<String, List<String[]>> data) throws IOException {

    XSSFWorkbook workBook = new XSSFWorkbook();

    for (Entry<String, List<String[]>> modelEntry : data.entrySet()) {
      XSSFSheet sheet = workBook.createSheet(modelEntry.getKey());
      int count = 0;
      for (String[] recordArray : modelEntry.getValue()) {
        XSSFRow row = sheet.createRow(count);
        int cellCount = 0;
        for (String val : recordArray) {
          XSSFCell cell = row.createCell(cellCount);
          cell.setCellValue(val);
          cellCount++;
        }
        count++;
      }
    }

    File excelFile = MetaFiles.createTempFile("Data", ".xls").toFile();
    FileOutputStream out = new FileOutputStream(excelFile);
    workBook.write(out);
    out.close();

    return metaFiles.upload(excelFile);
  }
}
