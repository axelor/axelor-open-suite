/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.groupExport;

import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.GroupAdvancedExport;
import com.axelor.apps.base.db.GroupExport;
import com.axelor.apps.base.service.advancedExport.AdvancedExportService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupExportServiceImpl implements GroupExportService {

  private final Logger log = LoggerFactory.getLogger(GroupExportServiceImpl.class);

  private AdvancedExportService advancedExportService;
  private GroupExportConfigGenerator groupExportConfigGenerator;
  private List<File> exportingFiles = new ArrayList<>();
  private boolean isGenerateConfig = false;
  private File zipFile = null;

  @Inject
  public GroupExportServiceImpl(
      AdvancedExportService advanceExportService,
      GroupExportConfigGenerator groupExportConfigGenerator) {
    this.advancedExportService = advanceExportService;
    this.groupExportConfigGenerator = groupExportConfigGenerator;
  }

  @Override
  public MetaFile exportAdvanceExports(GroupExport groupExport)
      throws AxelorException, IOException {

    log.debug("Group Export : Start");

    MetaFile exportFile = null;
    isGenerateConfig = groupExport.getIsGenerateConfig();
    List<GroupAdvancedExport> groupAdvanceExportList = groupExport.getGroupAdvancedExportList();

    // Sorting GroupAdvanceList according to sequence provided
    Collections.sort(
        groupAdvanceExportList, (o1, o2) -> (o1.getSequence().compareTo(o2.getSequence())));

    log.debug("GenerateConfig : {}", isGenerateConfig);

    // To adding opening and close tag of config in xml
    if (isGenerateConfig) {
      groupExportConfigGenerator.initialize();
      this.exportAll(groupAdvanceExportList);
      groupExportConfigGenerator.endConfig();
    } else {
      this.exportAll(groupAdvanceExportList);
    }

    // Creating Zip File
    zipFile = this.zipFiles();

    if (zipFile != null) {
      try (FileInputStream inStream = new FileInputStream(zipFile)) {
        exportFile = Beans.get(MetaFiles.class).upload(inStream, this.getExportFileName());
        inStream.close();
        zipFile.delete();
      }
    }
    return exportFile;
  }

  protected void exportAll(List<GroupAdvancedExport> groupAdvanceExportList)
      throws AxelorException {

    for (GroupAdvancedExport groupAdvancedExport :
        CollectionUtils.emptyIfNull(groupAdvanceExportList)) {
      AdvancedExport advancedExport = groupAdvancedExport.getAdvancedExport();

      if (advancedExport != null) {
        File file =
            advancedExportService.export(
                advancedExport,
                advancedExportService.getFilterConditionRecords(
                    groupAdvancedExport.getAdvancedExport()),
                advancedExportService.CSV,
                isGenerateConfig);

        if (file != null) {
          if (isGenerateConfig) {
            groupExportConfigGenerator.addFileConfig(file, advancedExport);
          }
          exportingFiles.add(file);
        }
      }
    }
  }

  protected File zipFiles() {

    if (exportingFiles == null || exportingFiles.isEmpty()) {
      return null;
    }

    if (isGenerateConfig) {
      exportingFiles.add(groupExportConfigGenerator.getConfigFile());
    }

    FileOutputStream fileOutStream = null;
    ZipOutputStream zipOutStream = null;
    FileInputStream fileInputStream = null;

    try {
      zipFile = File.createTempFile("GroupExport", ".zip");

      fileOutStream = new FileOutputStream(zipFile);
      zipOutStream = new ZipOutputStream(new BufferedOutputStream(fileOutStream));
      for (File file : exportingFiles) {
        fileInputStream = new FileInputStream(file);
        ZipEntry entry = new ZipEntry(file.getName());

        zipOutStream.putNextEntry(entry);
        byte[] tmp = new byte[4 * 1024];
        int size = 0;
        while ((size = fileInputStream.read(tmp)) != -1) {
          zipOutStream.write(tmp, 0, size);
        }
        zipOutStream.flush();
        fileInputStream.close();
        file.delete();
      }
      zipOutStream.close();

    } catch (Exception e) {
      TraceBackService.trace(e);

    } finally {
      try {
        if (fileOutStream != null) {
          fileOutStream.close();
        }
      } catch (Exception e) {
        TraceBackService.trace(e);
      }
    }
    return zipFile;
  }

  private String getExportFileName() {
    StringBuilder fileName = new StringBuilder("Group Export - ");
    fileName.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyy hhmmss")));
    fileName.append(".zip");
    return fileName.toString();
  }
}
