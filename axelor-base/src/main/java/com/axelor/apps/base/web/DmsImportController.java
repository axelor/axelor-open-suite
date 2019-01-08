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
package com.axelor.apps.base.web;

import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.apache.tika.detect.AutoDetectReader;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmsImportController {

  private static final Logger log = LoggerFactory.getLogger(DmsImportController.class);

  @Inject private DMSFileRepository dmsFileRepo;

  @Inject private MetaFiles metaFiles;

  @Inject private MetaFileRepository metaFileRepo;

  public void importDMS(ActionRequest request, ActionResponse response) throws AxelorException {

    Map<String, Object> metaFileMap = (Map<String, Object>) request.getContext().get("importFile");

    MetaFile metaFile = metaFileRepo.find(Long.parseLong(metaFileMap.get("id").toString()));

    ZipInputStream zipInputStream = validateZip(metaFile);
    try {
      createDmsTree(zipInputStream);
      response.setFlash("File loaded successfully");
      response.setReload(true);
    } catch (IOException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "Error while processing zip file");
    } finally {
      try {
        zipInputStream.close();
      } catch (IOException e) {
      }
    }
  }

  private ZipInputStream validateZip(MetaFile metaFile) throws AxelorException {

    try {
      File file = MetaFiles.getPath(metaFile).toFile();
      ZipFile zipFile = new ZipFile(file);
      AutoDetectReader autoDetectReader = new AutoDetectReader(new FileInputStream(file));
      ZipInputStream zis =
          new ZipInputStream(new FileInputStream(file), autoDetectReader.getCharset());
      autoDetectReader.close();
      return zis;
    } catch (IOException | TikaException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          "Uploaded file is not a valid zip file");
    }
  }

  private void createDmsTree(ZipInputStream zipInputStream) throws IOException {

    ZipEntry zipEntry = zipInputStream.getNextEntry();

    Map<String, DMSFile> dmsMap = new HashMap<>();

    while (zipEntry != null) {
      DMSFile dmsFile = new DMSFile();
      String fileName = getFileName(zipEntry);
      dmsFile.setFileName(fileName);
      String parentName = getParentName(zipEntry, fileName);
      dmsFile.setParent(dmsMap.get(parentName));
      log.debug("Praent file: {}", dmsFile.getParent());
      if (zipEntry.isDirectory()) {
        dmsFile.setIsDirectory(true);
        dmsFile = saveDmsFile(dmsFile);
        dmsMap.put(zipEntry.getName(), dmsFile);
        log.debug("DMS Directory created: {}", dmsFile.getFileName());
      } else {
        MetaFile metaFile = metaFiles.upload(zipInputStream, fileName);
        dmsFile.setMetaFile(metaFile);
        dmsFile = saveDmsFile(dmsFile);
        log.debug("DMS File created: {}", dmsFile.getFileName());
      }

      zipEntry = zipInputStream.getNextEntry();
    }
  }

  private String getFileName(ZipEntry zipEntry) {

    String entryName = zipEntry.getName();
    String[] names = entryName.split(File.separator);

    return names[names.length - 1];
  }

  private String getParentName(ZipEntry zipEntry, String fileName) {

    String entryName = zipEntry.getName();

    return entryName.substring(0, entryName.lastIndexOf(File.separator + fileName) + 1);
  }

  @Transactional
  public DMSFile saveDmsFile(DMSFile dmsFile) {
    return dmsFileRepo.save(dmsFile);
  }
}
