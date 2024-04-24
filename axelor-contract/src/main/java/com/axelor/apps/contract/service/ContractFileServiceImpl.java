package com.axelor.apps.contract.service;

import com.axelor.apps.base.db.File;
import com.axelor.apps.base.db.repo.FileRepository;
import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ContractFileServiceImpl implements ContractFileService {
  protected FileRepository contractFileRepository;
  protected DMSFileRepository dmsFileRepository;
  protected DMSService dmsService;
  protected MetaFiles metaFiles;
  protected AppBaseService appBaseService;

  @Inject
  public ContractFileServiceImpl(
      FileRepository contractFileRepository,
      DMSFileRepository dmsFileRepository,
      DMSService dmsService,
      MetaFiles metaFiles,
      AppBaseService appBaseService) {
    this.contractFileRepository = contractFileRepository;
    this.dmsFileRepository = dmsFileRepository;
    this.dmsService = dmsService;
    this.metaFiles = metaFiles;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional
  public void setDMSFile(File contractFile) {
    MetaFile metaFile = contractFile.getMetaFile();
    dmsService.setDmsFile(metaFile, contractFile);
    contractFileRepository.save(contractFile);
  }

  @Override
  public String getInlineUrl(File contractFile) {
    MetaFile metaFile = contractFile.getMetaFile();
    DMSFile dmsFile = contractFile.getDmsFile();
    if (metaFile == null || dmsFile == null || !"application/pdf".equals(metaFile.getFileType())) {
      return "";
    }
    return dmsService.getInlineUrl(dmsFile);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void remove(List<Integer> contractFileIds) {
    for (Integer id : contractFileIds) {
      File contractFile = contractFileRepository.find(Long.parseLong(String.valueOf(id)));
      contractFileRepository.remove(contractFile);
    }
  }
}
