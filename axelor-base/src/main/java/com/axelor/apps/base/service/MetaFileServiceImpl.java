package com.axelor.apps.base.service;

import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.io.Files;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;

public class MetaFileServiceImpl implements MetaFileService {
  protected final MetaFiles metaFiles;

  @Inject
  public MetaFileServiceImpl(MetaFiles metaFiles) {
    this.metaFiles = metaFiles;
  }

  @Override
  public MetaFile copyMetaFile(MetaFile metaFile) throws IOException {
    String copiedFileName = Files.getNameWithoutExtension(metaFile.getFileName()) + "_copy";
    File copiedFile =
        File.createTempFile(copiedFileName, "." + Files.getFileExtension(metaFile.getFilePath()));
    Files.copy(new File(MetaFiles.getPath(metaFile).toString()), copiedFile);
    return metaFiles.upload(copiedFile);
  }
}
