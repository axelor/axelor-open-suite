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
package com.axelor.studio.service.module;

// import com.axelor.apps.base.db.App;

import com.axelor.apps.tool.file.CsvTool;
import com.axelor.data.csv.CSVBind;
import com.axelor.data.csv.CSVConfig;
import com.axelor.data.csv.CSVInput;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

public class ModuleExportDataInitService {

  public static final String DATA_INIT_DIR = "src/main/resources/data-init/";

  @Inject private ModuleExportService moduleExportService;

  private static final char CSV_SEPRATOR = ';';

  private static final String REMOTE_SCHEMA = "data-import_" + CSVConfig.VERSION + ".xsd";

  public void exportDataInit(String module, ZipOutputStream zipOut)
      throws IOException, AxelorException {

    String modulePrefix = getModulePrefix(module);

    CSVConfig csvConfig = new CSVConfig();
    csvConfig.setInputs(new ArrayList<>());

    //    addApps(module, zipOut, csvConfig);
    Beans.get(ModuleExportAppBuilderService.class).addAppBuilders(modulePrefix, zipOut, csvConfig);
    Beans.get(ModuleExportJsonModelService.class).addJsonModels(modulePrefix, zipOut, csvConfig);
    Beans.get(ModuleExportJsonModelService.class).addJsonFields(module, zipOut, csvConfig);
    Beans.get(ModuleExportViewBuilderService.class)
        .addViewBuilders(modulePrefix, zipOut, csvConfig);
    Beans.get(ModuleExportWkfService.class).exportWkf(module, zipOut, csvConfig);
    addInputConfig(csvConfig, zipOut);
  }

  //  private void addApps(String module, ZipOutputStream zipOut, CSVConfig csvConfig)
  //      throws IOException, AxelorException {
  //
  //    List<AppBuilder> appBuilders = appBuilderRepo.all().filter("self.isReal = true").fetch();
  //
  //    for (AppBuilder appBuilder : appBuilders) {
  //
  //      String fileName = getModulePrefix(module) + appBuilder.getCode() + ".csv";
  //
  //      CSVInput input =
  //          createCSVInput(
  //              fileName,
  //              addAppEntity(module, appBuilder, zipOut),
  //              "com.axelor.csv.script.ImportApp:importApp",
  //              null);
  //      addAppDependsBind(input);
  //      csvConfig.getInputs().add(input);
  //
  //      List<String[]> data = new ArrayList<>();
  //
  //      createAppRecord(appBuilder, data, zipOut);
  //
  //      addCsv(zipOut, fileName, APP_HEADER, data);
  //    }
  //  }

  public void exportExcelDataInit(
      String module,
      List<String[]> jsonFieldData,
      List<String[]> jsonModelData,
      ZipOutputStream zipOut)
      throws IOException {

    String modulePrefix = getModulePrefix(module);

    CSVConfig csvConfig = new CSVConfig();
    csvConfig.setInputs(new ArrayList<>());

    Beans.get(ModuleExportJsonModelService.class)
        .addJsonModelsFromExcel(modulePrefix, jsonModelData, zipOut, csvConfig);

    Beans.get(ModuleExportJsonModelService.class)
        .addJsonFieldsFromExcel(module, jsonFieldData, zipOut, csvConfig);

    addInputConfig(csvConfig, zipOut);
  }

  public CSVInput createCSVInput(String fileName, String typeName, String callable, String search) {

    CSVInput input = new CSVInput();
    input.setFileName(fileName);
    input.setSeparator(CSV_SEPRATOR);
    input.setTypeName(typeName);
    input.setCallable(callable);
    input.setSearch(search);
    input.setBindings(new ArrayList<>());

    return input;
  }

  public CSVBind createCSVBind(
      String column, String field, String search, String expression, Boolean update) {

    CSVBind bind = new CSVBind();
    bind.setColumn(column);
    bind.setField(field);
    bind.setSearch(search);
    bind.setExpression(expression);
    bind.setUpdate(update);

    return bind;
  }

  //  private String addAppEntity(String module, AppBuilder appBuilder, ZipOutputStream zipOut)
  //      throws AxelorException, IOException {
  //
  //    String model = "App" + appBuilder.getCode();
  //    String xml = modelBuilderService.build(model, module, App.class.getName());
  //    String filePath = ModuleExportService.DOMAIN_DIR + model + ".xml";
  //
  //    moduleExportService.addZipEntry(filePath, xml, zipOut);
  //
  //    return modelBuilderService.getModelFullName(module, model);
  //  }

  public void addCsv(ZipOutputStream zipOut, String fileName, String[] header, List<String[]> data)
      throws IOException {

    File file = MetaFiles.createTempFile(fileName.replace(".csv", ""), ".csv").toFile();
    CsvTool.csvWriter(file.getParent(), file.getName(), ';', '"', header, data);
    moduleExportService.addZipEntry(DATA_INIT_DIR + "input/" + fileName, file, zipOut);
  }

  public String getModulePrefix(String module) {

    if (!module.contains("-")) {
      return module;
    }

    return module.substring(module.indexOf("-") + 1) + "_";
  }

  private void addInputConfig(CSVConfig csvConfig, ZipOutputStream zipOut) throws IOException {

    XStream xStream = new XStream();
    xStream.processAnnotations(CSVConfig.class);
    String xml = prepareXML(xStream.toXML(csvConfig));

    moduleExportService.addZipEntry(DATA_INIT_DIR + "input-config.xml", xml, zipOut);
  }

  private String prepareXML(String xml) {

    xml = xml.replace("<csv-inputs>", "").replaceAll("</csv-inputs>", "");

    StringBuilder sb = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>\n");
    sb.append("<csv-inputs")
        .append(" xmlns='")
        .append(CSVConfig.NAMESPACE)
        .append("'")
        .append(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'")
        .append(" xsi:schemaLocation='")
        .append(CSVConfig.NAMESPACE)
        .append(" ")
        .append(CSVConfig.NAMESPACE + "/" + REMOTE_SCHEMA)
        .append("'")
        .append(">\n\n")
        .append(xml)
        .append("\n\n</csv-inputs>");

    return sb.toString();
  }
}
