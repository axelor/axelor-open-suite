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
package com.axelor.studio.service.excel.importer;

import com.axelor.common.FileUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.studio.exception.IExceptionMessage;
import com.axelor.studio.service.builder.ViewBuilderService;
import com.axelor.studio.service.module.ModuleExportDataInitService;
import com.axelor.studio.service.module.ModuleExportService;
import com.axelor.studio.service.module.ModuleImportService;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelImporterService {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String MODULE_PATTERN = "axelor(-[a-z]+)+$";

  private List<String[]> jsonFieldData = new ArrayList<>();

  private List<String[]> jsonModelData = new ArrayList<>();

  @Inject private MetaFiles metaFiles;

  @Inject private MetaFileRepository metaFileRepo;

  @Inject private ModuleExportService moduleExportService;

  @Inject private ModuleImportService moduleImportService;

  @Inject private CustomImporter customImporter;

  @Inject private RealImporter realImporter;

  @Inject private ViewBuilderService viewBuilderService;

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private MetaJsonModelRepository metaJsonModelRepo;

  @Transactional
  public MetaFile importExcel(String module, DataReaderService reader, MetaFile metaFile)
      throws AxelorException, IOException, JAXBException {

    if (metaFile == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.NO_INPUT_FILE));
    }

    String extension = FilenameUtils.getExtension(metaFile.getFileName());
    if (extension == null || !extension.equals("xlsx")) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INVALID_EXCEL));
    }

    if (!module.matches(MODULE_PATTERN)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INVALID_MODULE_ZIP));
    }

    reader.initialize(metaFile);

    File zipFile = createModule(module, reader);

    installModule(module, zipFile);

    metaFile = metaFiles.upload(zipFile);
    metaFile.setFileName(module + ".zip");
    metaFileRepo.save(metaFile);

    zipFile.delete();

    return metaFile;
  }

  private File createModule(String module, DataReaderService reader)
      throws IOException, JAXBException, AxelorException {

    File zipFile = MetaFiles.createTempFile(module, ".zip").toFile();
    ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));

    moduleExportService.addBuildGradle(module, zipOut);

    processSheet(reader, module, zipOut);

    Beans.get(ModuleExportDataInitService.class)
        .exportExcelDataInit(module, jsonFieldData, jsonModelData, zipOut);

    zipOut.close();

    return zipFile;
  }

  private void processSheet(DataReaderService reader, String module, ZipOutputStream zipOut)
      throws IOException, JAXBException, AxelorException {

    String[] keys = reader.getKeys();
    if (keys == null) {
      return;
    }
    Map<String, ObjectViews> viewMap = new HashMap<>();

    for (String key : keys) {
      if (!key.equals("Menu")) {

        log.debug("Importing sheet: {}", key);

        int totalLines = reader.getTotalLines(key);
        if (totalLines == 0) {
          continue;
        }

        String checkCustom = key.substring(key.indexOf("(") + 1, key.lastIndexOf(")"));

        switch (checkCustom) {
          case "Custom":
            customImporter.customImport(this, reader, key, jsonModelData, jsonFieldData);
            break;

          case "Real":
            realImporter.realImporter(this, module, reader, key, viewMap, zipOut);
            break;

          default:
            throw new AxelorException(
                TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(IExceptionMessage.NO_MODULE_DATA));
        }
      }
    }
    addView(module, zipOut, viewMap);
  }

  private void addView(String module, ZipOutputStream zipOut, Map<String, ObjectViews> viewMap)
      throws IOException, JAXBException {

    for (String model : viewMap.keySet()) {
      ObjectViews objectViews = viewMap.get(model);
      if (objectViews == null) {
        continue;
      }
      moduleExportService.addZipEntry(
          RealImporter.VIEW_DIR + model + ".xml",
          viewBuilderService.createXml(objectViews),
          zipOut);
    }
  }

  protected Map<String, String> createValMap(String[] row, String[] headers) {

    Map<String, String> valMap = new HashMap<>();
    if (headers != null) {
      for (int i = 0; i < row.length; i++) {
        if (headers.length <= i) {
          break;
        }
        valMap.put(headers[i], row[i]);
      }
    }
    return valMap;
  }

  protected Object getModelFromName(String model) {
    MetaModel metaModel = metaModelRepo.findByName(model);
    if (metaModel == null) {
      MetaJsonModel jsonModel = metaJsonModelRepo.findByName(model);
      return jsonModel;
    }
    return metaModel;
  }

  private void installModule(String module, File file)
      throws ZipException, AxelorException, IOException {

    moduleImportService.validateFile(file);

    File moduleDir = new File(moduleImportService.getModuleDir(), module);

    if (!moduleDir.exists()) {
      moduleDir.mkdirs();
    }

    ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));

    ZipEntry entry = zipInputStream.getNextEntry();

    while (entry != null) {
      String name = entry.getName();
      File entryFile = FileUtils.getFile(moduleDir.getAbsolutePath(), name.split("/"));
      if (!entryFile.exists()) {
        Files.createParentDirs(entryFile);
      }
      IOUtils.copy(zipInputStream, new FileOutputStream(entryFile));
      entry = zipInputStream.getNextEntry();
    }

    zipInputStream.close();
  }
}
