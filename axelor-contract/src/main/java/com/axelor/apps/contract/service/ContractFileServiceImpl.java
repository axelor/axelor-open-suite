package com.axelor.apps.contract.service;

import com.axelor.apps.base.db.File;
import com.axelor.apps.base.db.repo.FileRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ContractFileServiceImpl implements ContractFileService {
  protected FileRepository contractFileRepository;
  protected DMSFileRepository dmsFileRepository;

  protected MetaFiles metaFiles;
  protected AppBaseService appBaseService;

  @Inject
  public ContractFileServiceImpl(
      FileRepository contractFileRepository,
      DMSFileRepository dmsFileRepository,
      MetaFiles metaFiles,
      AppBaseService appBaseService) {
    this.contractFileRepository = contractFileRepository;
    this.dmsFileRepository = dmsFileRepository;
    this.metaFiles = metaFiles;
    this.appBaseService = appBaseService;
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
