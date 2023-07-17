package com.axelor.apps.hr.service;

import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.hr.db.EmployeeFile;
import com.axelor.apps.hr.db.repo.EmployeeFileRepository;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EmployeeFileDMSServiceImpl implements EmployeeFileDMSService {
  protected EmployeeFileRepository employeeFileRepository;
  protected DMSFileRepository dmsFileRepository;
  protected DMSService dmsService;

  @Inject
  public EmployeeFileDMSServiceImpl(
      EmployeeFileRepository employeeFileRepository,
      DMSFileRepository dmsFileRepository,
      DMSService dmsService) {
    this.employeeFileRepository = employeeFileRepository;
    this.dmsFileRepository = dmsFileRepository;
    this.dmsService = dmsService;
  }

  @Override
  @Transactional
  public void setDMSFile(EmployeeFile employeeFile) {
    MetaFile metaFile = employeeFile.getMetaFile();
    Long metaFileId = employeeFile.getMetaFileId();
    Long dmsId = dmsService.setDmsFile(metaFile, metaFileId, employeeFile);

    if (metaFile == null) {
      employeeFile.setMetaFileId(null);
    }
    if (dmsId != null) {
      employeeFile.setMetaFileId(dmsId);
    }

    employeeFileRepository.save(employeeFile);
  }

  @Override
  public String getInlineUrl(EmployeeFile employeeFile) {
    MetaFile metaFile = employeeFile.getMetaFile();
    if (metaFile == null || !"application/pdf".equals(metaFile.getFileType())) {
      return "";
    }
    DMSFile dmsFile = dmsFileRepository.find(employeeFile.getMetaFileId());
    return dmsService.getInlineUrl(dmsFile.getId());
  }
}
