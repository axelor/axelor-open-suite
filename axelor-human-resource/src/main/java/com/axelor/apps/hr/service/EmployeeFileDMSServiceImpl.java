package com.axelor.apps.hr.service;

import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.hr.db.EmployeeFile;
import com.axelor.apps.hr.db.repo.EmployeeFileRepository;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EmployeeFileDMSServiceImpl implements EmployeeFileDMSService {
  protected EmployeeFileRepository employeeFileRepository;
  protected DMSFileRepository dmsFileRepository;
  protected DMSService dmsService;
  protected MetaFiles metaFiles;

  @Inject
  public EmployeeFileDMSServiceImpl(
      EmployeeFileRepository employeeFileRepository,
      DMSFileRepository dmsFileRepository,
      DMSService dmsService,
      MetaFiles metaFiles) {
    this.employeeFileRepository = employeeFileRepository;
    this.dmsFileRepository = dmsFileRepository;
    this.dmsService = dmsService;
    this.metaFiles = metaFiles;
  }

  @Override
  @Transactional
  public void setDMSFile(EmployeeFile employeeFile) {
    MetaFile metaFile = employeeFile.getMetaFile();
    dmsService.setDmsFile(metaFile, employeeFile);
    employeeFileRepository.save(employeeFile);
  }

  @Override
  public String getInlineUrl(EmployeeFile employeeFile) {
    MetaFile metaFile = employeeFile.getMetaFile();
    DMSFile dmsFile = employeeFile.getDmsFile();
    if (metaFile == null || dmsFile == null || !"application/pdf".equals(metaFile.getFileType())) {
      return "";
    }
    return dmsService.getInlineUrl(dmsFile);
  }
}
