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
package com.axelor.apps.base.service;

import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.tika.detect.AutoDetectReader;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMSImportWizardServiceImpl implements DMSImportWizardService {

  @Inject private MetaFiles metaFiles;

  private Logger LOG = LoggerFactory.getLogger(getClass());

  @Override
  public void importDMS(MetaFile metaFile) throws AxelorException {
    try {
      File file = MetaFiles.getPath(metaFile).toFile();

      try (ZipFile zipFile = new ZipFile(file);
          ZipInputStream zipInputStream = validateZip(file)) {
        createDmsTree(zipInputStream, zipFile);
      }
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.DMS_IMPORT_PROCESS_SUCCESS_MESSAGE));
    } catch (IOException | UnsupportedOperationException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.DMS_IMPORT_FILE_PROCESS_ERROR));
    }
  }

  private ZipInputStream validateZip(File file) throws AxelorException {
    try (AutoDetectReader autoDetectReader = new AutoDetectReader(new FileInputStream(file));
        ZipInputStream zis =
            new ZipInputStream(new FileInputStream(file), autoDetectReader.getCharset())) {
      return zis;
    } catch (IOException | TikaException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.DMS_IMPORT_INVALID_ZIP_ERROR));
    }
  }

  @Transactional
  public void createDmsTree(ZipInputStream zipInputStream, ZipFile zipfile) throws IOException {
    ZipEntry zipEntry = zipInputStream.getNextEntry();
    Map<String, DMSFile> dmsMap = new HashMap<>();
    while (zipEntry != null) {
      String fileName = getFileName(zipEntry);
      DMSFile dmsFile = new DMSFile();
      dmsFile.setFileName(fileName);
      String parentName = getParentName(zipEntry, fileName);
      dmsFile.setParent(dmsMap.get(parentName));
      LOG.debug("Praent file: {}", dmsFile.getParent());
      if (zipEntry.isDirectory()) {
        dmsFile.setIsDirectory(true);
        dmsFile = Beans.get(DMSFileRepository.class).save(dmsFile);
        dmsMap.put(zipEntry.getName(), dmsFile);
        LOG.debug("DMS Directory created: {}", dmsFile.getFileName());
      } else {
        String fileType = fileName.substring(fileName.indexOf(".") + 1);
        try {
          File tempDir = File.createTempFile("", "");
          File file = new File(tempDir, fileName);
          InputStream is = zipfile.getInputStream(zipEntry);
          FileUtils.copyInputStreamToFile(is, file);
          MetaFiles.checkType(file);
          MetaFile metaFile = metaFiles.upload(zipInputStream, fileName);
          dmsFile.setMetaFile(metaFile);
          dmsFile = Beans.get(DMSFileRepository.class).save(dmsFile);
          LOG.debug("DMS File created: {}", dmsFile.getFileName());
        } catch (IllegalArgumentException e) {
          LOG.debug("File type is not allowed : {}", fileType);
        }
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
}
