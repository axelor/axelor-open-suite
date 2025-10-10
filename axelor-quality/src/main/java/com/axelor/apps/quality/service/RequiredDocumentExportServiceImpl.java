package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.quality.db.RequiredDocument;
import com.axelor.apps.quality.db.repo.RequiredDocumentRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;

public class RequiredDocumentExportServiceImpl implements RequiredDocumentExportService {

  protected final RequiredDocumentRepository requiredDocRepo;

  @Inject
  public RequiredDocumentExportServiceImpl(RequiredDocumentRepository requiredDocRepo) {
    this.requiredDocRepo = requiredDocRepo;
  }

  @Override
  public Path exportFile(List<Integer> idList) throws AxelorException, IOException {
    List<File> files = new ArrayList<>();
    List<RequiredDocument> requiredDocuments =
        requiredDocRepo.findByIds(idList.stream().map(Long::valueOf).collect(Collectors.toList()));

    for (RequiredDocument requiredDocument : requiredDocuments) {
      MetaFile metaFile = requiredDocument.getMetaFile();
      if (metaFile == null) {
        continue;
      }
      File file = MetaFiles.getPath(metaFile).toFile();
      if (file.exists()) {
        files.add(file);
      }
    }

    if (CollectionUtils.isEmpty(files)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(QualityExceptionMessage.NO_RECORD_SELECTED_TO_EXPORT));
    }

    return createZip(files);
  }

  protected Path createZip(List<File> fileList) throws IOException {
    if (CollectionUtils.isEmpty(fileList)) {
      return null;
    }
    Path zipFile = MetaFiles.createTempFile("Required documents", ".zip");

    try (ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(zipFile))) {
      for (File file : fileList) {
        zout.putNextEntry(new ZipEntry(file.getName()));
        zout.write(IOUtils.toByteArray(Files.newInputStream(file.toPath())));
      }
    }
    return zipFile;
  }
}
