package com.axelor.apps.base.service;

import com.axelor.apps.base.db.File;
import com.axelor.apps.base.db.repo.FileRepository;
import com.axelor.dms.db.DMSFile;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class FileServiceImpl implements FileService {

  protected DMSService dmsService;
  protected FileRepository contractFileRepository;

  @Inject
  public FileServiceImpl(DMSService dmsService, FileRepository contractFileRepository) {
    this.dmsService = dmsService;
    this.contractFileRepository = contractFileRepository;
  }

  @Override
  @Transactional
  public void setDMSFile(File file) {
    MetaFile metaFile = file.getMetaFile();
    dmsService.setDmsFile(metaFile, file);
    contractFileRepository.save(file);
  }

  @Override
  public String getInlineUrl(File file) {
    MetaFile metaFile = file.getMetaFile();
    DMSFile dmsFile = file.getDmsFile();
    if (metaFile == null || dmsFile == null || !"application/pdf".equals(metaFile.getFileType())) {
      return "";
    }
    return dmsService.getInlineUrl(dmsFile);
  }
}
