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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections4.CollectionUtils;

public class GroupExportServiceImpl implements GroupExportService {

  private AdvancedExportService advancedExportService;
  private MetaFiles metaFiles;
  List<File> exportingFiles = new ArrayList<>();
  boolean isGenerateConfig = false;
  File zipFile = null;

  @Inject
  public GroupExportServiceImpl(AdvancedExportService advanceExportService, MetaFiles metaFiles) {
    this.advancedExportService = advanceExportService;
    this.metaFiles = metaFiles;
  }

  @Override
  public MetaFile exportAdvanceExports(GroupExport groupExport)
      throws AxelorException, IOException {

    List<GroupAdvancedExport> groupAdvanceExportList = groupExport.getGroupAdvancedExportList();
    Collections.sort(
        groupAdvanceExportList, (o1, o2) -> (o1.getSequence().compareTo(o2.getSequence())));

    isGenerateConfig = groupExport.getIsGenerateConfig();
    MetaFile exportFile = null;

    this.exportAll(groupAdvanceExportList);
    zipFile = this.zipFiles();

    if (zipFile != null) {
      try (FileInputStream inStream = new FileInputStream(zipFile)) {
        exportFile = Beans.get(MetaFiles.class).upload(inStream, "Data.zip");
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

      File file =
          advancedExportService.export(
              advancedExport,
              advancedExportService.getFilterConditionRecords(
                  groupAdvancedExport.getAdvancedExport()),
              advancedExportService.CSV,
              isGenerateConfig);

      if (file != null) {
        exportingFiles.add(file);
      }
    }
  }

  public File zipFiles() {

    if (exportingFiles == null || exportingFiles.isEmpty()) {
      return null;
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
}
